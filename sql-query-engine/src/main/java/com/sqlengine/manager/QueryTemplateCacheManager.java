package com.sqlengine.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sqlengine.dto.CachedQueryTemplate;
import com.sqlengine.model.QueryTemplate;
import com.sqlengine.repository.QueryTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueryTemplateCacheManager {

    private final QueryTemplateRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Blocking cache for service use
    private final Cache<String, CachedQueryTemplate> cache = Caffeine.newBuilder()
            .expireAfterAccess(60, TimeUnit.MINUTES)
            .maximumSize(500)
            .build(this::loadFromMongo);

    /**
     * Cache loader (used only in blocking getById)
     */
    private CachedQueryTemplate loadFromMongo(String templateId) {
        QueryTemplate template = repository.findById(templateId).block();
        if (template == null) {
            throw new IllegalArgumentException("QueryTemplate not found with ID: " + templateId);
        }
        return createCached(template);
    }

    /**
     * Wrap and hash template
     */
    private CachedQueryTemplate createCached(QueryTemplate template) {
        try {
            String hash = DigestUtils.sha256Hex(objectMapper.writeValueAsString(template));
            return new CachedQueryTemplate(template, hash);
        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to create cache entry for QueryTemplate ID: " + template.getId(), e);
        }
    }

    /**
     * Blocking get by ID with fallback to DB
     */
    public QueryTemplate getById(String templateId) {
        CachedQueryTemplate cached = cache.getIfPresent(templateId);
        if (cached != null) {
            log.debug("✅ Returning cached QueryTemplate for ID: {}", templateId);
            return cached.getTemplate();
        }

        log.info("📥 Cache miss. Loading QueryTemplate from MongoDB for ID: {}", templateId);
        return cache.get(templateId, key -> loadFromMongo(templateId)).getTemplate();
    }

    /**
     * Reactive get by ID with fallback and cache update
     */
    public Mono<QueryTemplate> getByIdReactive(String templateId) {
        CachedQueryTemplate cached = cache.getIfPresent(templateId);
        if (cached != null) {
            log.debug("✅ Returning cached QueryTemplate for ID: {}", templateId);
            return Mono.just(cached.getTemplate());
        }

        return repository.findById(templateId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("QueryTemplate not found: " + templateId)))
                .map(template -> {
                    CachedQueryTemplate wrapped = createCached(template);
                    cache.put(templateId, wrapped);
                    return template;
                });
    }

    /**
     * Update if hash has changed
     */
    public QueryTemplate get(QueryTemplate currentTemplate) {
        String id = currentTemplate.getId();
        CachedQueryTemplate cached = cache.getIfPresent(id);
        String currentHash = hash(currentTemplate);

        if (cached != null && cached.getHash().equals(currentHash)) {
            log.debug("✅ Returning up-to-date cached QueryTemplate: {}", currentTemplate.getTemplateName());
            return cached.getTemplate();
        }

        CachedQueryTemplate updated = createCached(currentTemplate);
        cache.put(id, updated);
        log.info("♻️ Updated cache for QueryTemplate: {}", currentTemplate.getTemplateName());
        return updated.getTemplate();
    }

    /**
     * Manual preload
     */
    public void preload(QueryTemplate template) {
        cache.put(template.getId(), createCached(template));
        log.info("⚡ Preloaded QueryTemplate: {}", template.getTemplateName());
    }

    /**
     * Manual eviction
     */
    public void evict(String templateId) {
        cache.invalidate(templateId);
        log.info("🧹 Evicted QueryTemplate from cache: {}", templateId);
    }

    /**
     * Generate hash for comparison
     */
    private String hash(QueryTemplate template) {
        try {
            return DigestUtils.sha256Hex(objectMapper.writeValueAsString(template));
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute hash for QueryTemplate", e);
        }
    }
}
