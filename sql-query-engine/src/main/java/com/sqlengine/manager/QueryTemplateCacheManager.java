package com.sqlengine.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sqlengine.dto.CachedQueryTemplate;
import com.sqlengine.model.QueryTemplate;
import com.sqlengine.repository.QueryTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class QueryTemplateCacheManager {

    private final QueryTemplateRepository repository;
    private ObjectMapper objectMapper = new ObjectMapper();

    public QueryTemplateCacheManager(QueryTemplateRepository repository){

        this.repository = repository;
        this.objectMapper = objectMapper.registerModule(new JavaTimeModule());
    }

    private final AsyncCache<String, CachedQueryTemplate> asyncCache = Caffeine.newBuilder()
            .expireAfterAccess(60, TimeUnit.MINUTES)
            .maximumSize(500)
            .buildAsync((templateId, executor) -> loadFromMongo(templateId).toFuture());

    /**
     * Load from MongoDB reactively
     */
    private Mono<CachedQueryTemplate> loadFromMongo(String templateId) {
        log.info("📥 Cache miss. Loading QueryTemplate from MongoDB for ID: {}", templateId);
        return repository.findById(templateId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("QueryTemplate not found with ID: " + templateId)))
                .map(this::createCached);
    }

    /**
     * Reactive get by ID using async cache
     */
    public Mono<QueryTemplate> getById(String templateId) {
        return Mono.fromFuture(
                asyncCache.get(templateId, (key, executor) -> loadFromMongo(key).toFuture())
        ).map(CachedQueryTemplate::getTemplate);
    }



    /**
     * Reactive get from cache if present (non-blocking)
     */
    public Mono<QueryTemplate> getByIdIfCached(String templateId) {
        return Mono.justOrEmpty(asyncCache.synchronous().getIfPresent(templateId))
                .map(CachedQueryTemplate::getTemplate);
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
     * Manual update if content changed
     */
    public Mono<QueryTemplate> get(QueryTemplate currentTemplate) {
        String id = currentTemplate.getId();
        String currentHash = hash(currentTemplate);
        CachedQueryTemplate cached = asyncCache.synchronous().getIfPresent(id);

        if (cached != null && cached.getHash().equals(currentHash)) {
            log.debug("✅ Returning up-to-date cached QueryTemplate: {}", currentTemplate.getTemplateName());
            return Mono.just(cached.getTemplate());
        }

        CachedQueryTemplate updated = createCached(currentTemplate);
        asyncCache.put(id, CompletableFuture.completedFuture(updated));
        log.info("♻️ Updated cache for QueryTemplate: {}", currentTemplate.getTemplateName());
        return Mono.just(updated.getTemplate());
    }

    /**
     * Manual preload
     */
    public void preload(QueryTemplate template) {
        asyncCache.put(template.getId(), CompletableFuture.completedFuture(createCached(template)));
        log.info("⚡ Preloaded QueryTemplate: {}", template.getTemplateName());
    }

    /**
     * Manual eviction
     */
    public void evict(String templateId) {
        asyncCache.synchronous().invalidate(templateId);
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
