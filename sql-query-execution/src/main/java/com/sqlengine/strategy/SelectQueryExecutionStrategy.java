package com.sqlengine.strategy;

import com.sqlengine.enums.JoinType;
import com.sqlengine.manager.TableMetadataManager;
import com.sqlengine.model.DatabaseConfig;
import com.sqlengine.model.QueryTemplate;
import com.sqlengine.model.query.CteBlock;
import com.sqlengine.model.query.JoinConfig;
import com.sqlengine.model.query.QueryCondition;
import com.sqlengine.model.query.SelectedColumn;
import com.sqlengine.model.query.SubqueryBlock;
import com.sqlengine.model.query.UnionQuery;
import com.sqlengine.utils.QueryParamCaster;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.Parameter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

@Slf4j
@Component("select")
@RequiredArgsConstructor
public class SelectQueryExecutionStrategy implements QueryExecutionStrategy {

    private final TableMetadataManager tableMetadataManager;

    @Override
    public String getType() {
        return "SELECT";
    }

    @Override
    public Mono<Object> execute(QueryTemplate template, DatabaseConfig config, DatabaseClient dbClient) {
        return runWithMetadata(template, config, dbClient, false)
                .onErrorResume(ex -> {
                    log.warn("⚠️ Initial query failed. Retrying with fresh metadata: {}", ex.getMessage());
                    tableMetadataManager.invalidate(config, template.getTableName());
                    return runWithMetadata(template, config, dbClient, true);
                });
    }

    private Mono<Object> runWithMetadata(QueryTemplate template, DatabaseConfig config, DatabaseClient dbClient, boolean isRetry) {
        return tableMetadataManager.getColumnTypesReactive(config, dbClient, template.getTableName())
                .flatMap(columnTypes -> {
                    Map<String, Object> params = new HashMap<>();
                    StringBuilder sql = new StringBuilder();

                    // CTE Block (WITH ...)
                    if (template.getCtes() != null && !template.getCtes().isEmpty()) {
                        sql.append("WITH ");
                        StringJoiner cteJoiner = new StringJoiner(", ");
                        for (CteBlock cte : template.getCtes()) {
                            if (!StringUtils.hasText(cte.getName()) || !StringUtils.hasText(cte.getQuery())) {
                                return Mono.error(new IllegalArgumentException("CTE block must have both name and query"));
                            }
                            cteJoiner.add(cte.getName() + " AS (" + cte.getQuery() + ")");
                        }
                        sql.append(cteJoiner).append(" ");
                    }

                    String baseQuery = buildSingleQuery(template, columnTypes, params);
                    sql.append(baseQuery);

                    // Union queries (fallback to blocking metadata call for unions)
                    if (template.getUnions() != null && !template.getUnions().isEmpty()) {
                        for (UnionQuery union : template.getUnions()) {
                            QueryTemplate unionTemplate = union.getTemplate();
                            Map<String, Integer> unionColumnTypes = tableMetadataManager.getColumnTypes(config, null, unionTemplate.getTableName()); // ⚠️ fallback to blocking
                            Map<String, Object> unionParams = new HashMap<>();
                            String unionSql = buildSingleQuery(unionTemplate, unionColumnTypes, unionParams);
                            sql.append(union.isUnionAll() ? " UNION ALL " : " UNION ").append(unionSql);
                            params.putAll(unionParams);
                        }
                    }

                    if (isRetry) {
                        log.info("🔁 Retrying with refreshed metadata...");
                    }

                    log.debug("🟢 Final SQL: {}", sql);

                    DatabaseClient.GenericExecuteSpec spec = dbClient.sql(sql.toString());
                    for (Map.Entry<String, Object> entry : params.entrySet()) {
                        spec = spec.bind(entry.getKey(), Parameter.fromOrEmpty(entry.getValue(), Object.class));
                    }

                    return spec.fetch().all().collectList().cast(Object.class);
                });
    }


    private String buildSingleQuery(QueryTemplate template, Map<String, Integer> columnTypes, Map<String, Object> params) {
        StringBuilder sql = new StringBuilder();

        if (template.isDistinct()) {
            sql.append("SELECT DISTINCT ");
        } else {
            sql.append("SELECT ");
        }

        if (template.getSelectedColumns() == null || template.getSelectedColumns().isEmpty()) {
            sql.append("*");
        } else {
            StringJoiner selectJoiner = new StringJoiner(", ");
            for (SelectedColumn col : template.getSelectedColumns()) {
                if (StringUtils.hasText(col.getAlias())) {
                    selectJoiner.add(col.getExpression() + " AS " + col.getAlias());
                } else {
                    selectJoiner.add(col.getExpression());
                }
            }
            sql.append(selectJoiner);
        }

        if (template.getSubqueries() != null && !template.getSubqueries().isEmpty()) {
            for (SubqueryBlock sub : template.getSubqueries()) {
                if (!StringUtils.hasText(sub.getName()) || !StringUtils.hasText(sub.getQuery())) {
                    throw new IllegalArgumentException("Subquery must have both name and query.");
                }
                sql.append("( ").append(sub.getQuery()).append(" ) AS ").append(sub.getName());
            }
        }

        sql.append(" FROM ").append(template.getTableName());

        if (template.getJoins() != null && !template.getJoins().isEmpty()) {
            for (JoinConfig join : template.getJoins()) {
                if (JoinType.CROSS.equals(join.getJoinType())) {
                    sql.append(" CROSS JOIN ").append(join.getTable());
                } else {
                    sql.append(" ").append(join.getJoinType()).append(" JOIN ")
                            .append(join.getTable());
                    if (StringUtils.hasText(join.getAlias())) {
                        sql.append(" ").append(join.getAlias());
                    }
                    sql.append(" ON ").append(join.getOnCondition());
                }
            }
        }

        if (template.getConditions() != null && !template.getConditions().isEmpty()) {
            StringJoiner whereJoiner = new StringJoiner(" ");
            for (QueryCondition condition : template.getConditions()) {
                String field = condition.getFieldName().toLowerCase();
                Object rawValue = condition.getValue();
                if (!columnTypes.containsKey(field)) continue;
                int sqlType = columnTypes.get(field);
                try {
                    Object casted = QueryParamCaster.cast(rawValue, sqlType);
                    params.put(field, casted);
                } catch (Exception ex) {
                    String message = String.format("❌ Invalid value '%s' for field '%s'. Expected SQL type: %s",
                            rawValue, field, sqlType);
                    log.error(message);
                    throw new IllegalArgumentException(message, ex);
                }

                whereJoiner.add(field + " " + condition.getOperator().getSymbol() + " :" + field);
                if (condition.getFilterOperator() != null) {
                    whereJoiner.add(condition.getFilterOperator());
                }
            }
            sql.append(" WHERE ").append(whereJoiner);
        }

        if (template.getGroupBy() != null && !template.getGroupBy().isEmpty()) {
            for (String groupField : template.getGroupBy()) {
                if (!columnTypes.containsKey(groupField.toLowerCase())) {
                    throw new IllegalArgumentException("❌ Invalid groupBy column: " + groupField);
                }
            }

            sql.append(" GROUP BY ");
            sql.append(String.join(", ", template.getGroupBy()));
        }


        if (template.getHavingConditions() != null && !template.getHavingConditions().isEmpty()) {
            StringJoiner havingJoiner = new StringJoiner(" ");
            for (QueryCondition condition : template.getHavingConditions()) {
                String field = condition.getFieldName().toLowerCase();
                Object rawValue = condition.getValue();
                try {
                    Object casted = QueryParamCaster.cast(rawValue, Types.VARCHAR);
                    params.put("having_" + field, casted);
                } catch (Exception ex) {
                    throw new IllegalArgumentException("❌ Invalid HAVING value for field: " + field, ex);
                }
                havingJoiner.add(field + " " + condition.getOperator().getSymbol() + " :" + "having_" + field);
                if (condition.getFilterOperator() != null) {
                    havingJoiner.add(condition.getFilterOperator());
                }
            }
            sql.append(" HAVING ").append(havingJoiner);
        }

        if (template.getOrderBy() != null && !template.getOrderBy().isEmpty()) {
            StringJoiner orderJoiner = new StringJoiner(", ");
            template.getOrderBy().forEach((column, direction) -> {
                orderJoiner.add(column + " " + direction.name());
            });
            sql.append(" ORDER BY ").append(orderJoiner);
        }

        if (template.getLimit() != null) {
            sql.append(" LIMIT ").append(template.getLimit());
        }

        if (template.getOffset() != null) {
            sql.append(" OFFSET ").append(template.getOffset());
        }

        return sql.toString();
    }

}

