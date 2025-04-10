package com.sqlengine.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sqlengine.manager.QueryTemplateCacheManager;
import com.sqlengine.model.QueryTemplate;
import com.sqlengine.model.query.CteBlock;
import com.sqlengine.model.query.JoinConfig;
import com.sqlengine.model.query.QueryCondition;
import com.sqlengine.model.query.SelectedColumn;
import com.sqlengine.repository.QueryTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueryTemplateService {

    private final QueryTemplateRepository repository;
    private final QueryTemplateCacheManager cacheManager;

    public Mono<QueryTemplate> save(QueryTemplate template) {
        return repository.findByTemplateName(template.getTemplateName())
                .flatMap(existing -> Mono.<QueryTemplate>error(
                        new IllegalArgumentException("QueryTemplate already exists: " + template.getTemplateName())))
                .switchIfEmpty(Mono.defer(() -> {
                    validate(template);
                    template.setCreatedAt(LocalDateTime.now());
                    template.setUpdatedAt(LocalDateTime.now());
                    return repository.save(template)
                            .doOnNext(cacheManager::preload);
                }));
    }

    public Mono<QueryTemplate> findById(String id) {
        return repository.findById(id);
    }

    public Mono<QueryTemplate> findByTemplateName(String templateName) {
        return repository.findByTemplateName(templateName);
    }

    public Flux<QueryTemplate> findAll() {
        return repository.findAll();
    }

    public Mono<Void> deleteById(String id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("QueryTemplate not found with ID: " + id)))
                .flatMap(existing -> repository.deleteById(id)
                        .doOnSuccess(unused -> cacheManager.evict(id)));
    }

    public Mono<QueryTemplate> update(String id, QueryTemplate updated) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("QueryTemplate not found with ID: " + id)))
                .flatMap(existing -> {
                    if (!existing.getTemplateName().equals(updated.getTemplateName())) {
                        return Mono.error(new IllegalArgumentException("Template name cannot be changed."));
                    }

                    validate(updated);
                    updated.setId(id);
                    updated.setCreatedAt(existing.getCreatedAt());
                    updated.setUpdatedAt(LocalDateTime.now());

                    return repository.save(updated)
                            .doOnSuccess(saved -> {
                                cacheManager.evict(id);
                                cacheManager.preload(saved);
                            });
                });
    }

    private void validate(QueryTemplate template) {
        if (!StringUtils.hasText(template.getTemplateName())) {
            throw new IllegalArgumentException("Template name must not be empty.");
        }

        if (!StringUtils.hasText(template.getQueryType())) {
            throw new IllegalArgumentException("Query type must be provided.");
        }

        if (!StringUtils.hasText(template.getTableName())) {
            throw new IllegalArgumentException("From table must be provided.");
        }

        if (template.getLimit() != null && template.getLimit() <= 0) {
            throw new IllegalArgumentException("Limit must be a positive number.");
        }

        if (template.getOffset() != null && template.getOffset() < 0) {
            throw new IllegalArgumentException("Offset cannot be negative.");
        }

        if (template.getSelectedColumns() == null || template.getSelectedColumns().isEmpty()) {
            throw new IllegalArgumentException("At least one selected column must be provided.");
        }

        for (SelectedColumn col : template.getSelectedColumns()) {
            if (!StringUtils.hasText(col.getExpression())) {
                throw new IllegalArgumentException("Selected column expression must not be empty.");
            }

            // Optional unsafe keyword check
            String expr = col.getExpression().toUpperCase();
            if (expr.contains(";") || expr.matches(".*\\b(DROP|DELETE|TRUNCATE)\\b.*")) {
                throw new IllegalArgumentException("Unsafe expression detected in selected column: " + col.getExpression());
            }
        }

        // DISTINCT + GROUP BY warning
        if (template.isDistinct() && template.getGroupBy() != null && !template.getGroupBy().isEmpty()) {
            log.warn("⚠️ DISTINCT used with GROUP BY. This may be redundant or conflicting.");
        }

        // Validate JOINs
        if (template.getJoins() != null && !template.getJoins().isEmpty()) {
            Set<String> aliases = new HashSet<>();
            aliases.add(template.getTableName().toLowerCase());

            for (JoinConfig join : template.getJoins()) {
                String joinType = join.getJoinType() != null ? String.valueOf(join.getJoinType()).toUpperCase() : "";

                if (!StringUtils.hasText(join.getTable())) {
                    throw new IllegalArgumentException("Join table is missing in one of the JOIN definitions.");
                }

                if (!Arrays.asList("INNER", "LEFT", "RIGHT", "FULL", "CROSS").contains(joinType)) {
                    throw new IllegalArgumentException("Invalid join type: '" + joinType + "'. Allowed: INNER, LEFT, RIGHT, FULL, CROSS.");
                }

                if ("CROSS".equals(joinType)) {
                    if (StringUtils.hasText(join.getOnCondition())) {
                        throw new IllegalArgumentException("CROSS JOIN must not have an ON condition.");
                    }
                } else {
                    if (!StringUtils.hasText(join.getOnCondition())) {
                        throw new IllegalArgumentException(joinType + " JOIN requires an ON condition with table '" + join.getTable() + "'.");
                    }

                    if (join.getTable().equalsIgnoreCase(template.getTableName())) {
                        if (!StringUtils.hasText(join.getAlias())) {
                            throw new IllegalArgumentException("Self-join detected for table '" + join.getTable() + "'. Alias is required.");
                        }
                        if (join.getAlias().equalsIgnoreCase(template.getTableName())) {
                            throw new IllegalArgumentException("Self-join alias cannot match base table name.");
                        }
                    }
                }

                if (StringUtils.hasText(join.getAlias())) {
                    String aliasLower = join.getAlias().toLowerCase();
                    if (!aliases.add(aliasLower)) {
                        throw new IllegalArgumentException("Duplicate table alias in JOINs: " + aliasLower);
                    }
                }
            }
        }

        // Validate CTEs
        if (template.getCtes() != null && !template.getCtes().isEmpty()) {
            for (CteBlock cte : template.getCtes()) {
                if (!StringUtils.hasText(cte.getName()) || !StringUtils.hasText(cte.getQuery())) {
                    throw new IllegalArgumentException("CTE block must have both 'name' and 'query'.");
                }
            }
        }

        // Validate GROUP BY columns are in selectedColumns
        if (template.getGroupBy() != null && !template.getGroupBy().isEmpty()) {
            List<String> selectedExprs = template.getSelectedColumns().stream()
                    .map(SelectedColumn::getExpression)
                    .map(String::toLowerCase)
                    .toList();

            for (String groupCol : template.getGroupBy()) {
                if (!selectedExprs.contains(groupCol.toLowerCase())) {
                    throw new IllegalArgumentException("Group by column '" + groupCol + "' must be in selectedColumns.");
                }
            }
        }

        // HAVING requires GROUP BY
        if (template.getHavingConditions() != null && !template.getHavingConditions().isEmpty()) {
            if (template.getGroupBy() == null || template.getGroupBy().isEmpty()) {
                throw new IllegalArgumentException("HAVING conditions require GROUP BY clause.");
            }
        }

        // Optional: IN clause size cap
        if (template.getConditions() != null) {
            for (QueryCondition condition : template.getConditions()) {
                if ("IN".equalsIgnoreCase(condition.getOperator().name())) {
                    try {
                        List<?> list = new ObjectMapper().readValue(condition.getValue(), List.class);
                        if (list.size() > 1000) {
                            throw new IllegalArgumentException("IN clause exceeds allowed limit (1000 values max).");
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }



}
