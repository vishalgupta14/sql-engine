package com.sqlengine.strategy;

import com.sqlengine.enums.DatabaseProvider;
import com.sqlengine.manager.TableMetadataManager;
import com.sqlengine.model.DatabaseConfig;
import com.sqlengine.model.QueryTemplate;
import com.sqlengine.model.query.JoinConfig;
import com.sqlengine.model.query.QueryCondition;
import com.sqlengine.utils.QueryParamCaster;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.Parameter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component("update")
@RequiredArgsConstructor
public class UpdateQueryExecutionStrategy implements QueryExecutionStrategy {

    private final TableMetadataManager tableMetadataManager;
    private static final int BATCH_THRESHOLD = 500;
    private static final int PARALLELISM = 4;

    @Override
    public String getType() {
        return "UPDATE";
    }

    @Override
    public Mono<Object> execute(QueryTemplate template, DatabaseConfig config, DatabaseClient dbClient) {
        return tableMetadataManager.getColumnTypesReactive(config, dbClient, template.getTableName())
                .flatMap(columnTypes -> {
                    DatabaseProvider provider = config.getProvider();
                    Map<String, Object> params = new HashMap<>();
                    StringBuilder sql = new StringBuilder();

                    if (template.getJoins() != null && !template.getJoins().isEmpty()) {
                        return emulateJoinUpdateForAllDBs(template, config, dbClient, columnTypes);
                    } else {
                        sql.append(buildSimpleUpdateSQL(template, columnTypes, params));
                        log.debug("🔄 Generated UPDATE SQL: {}", sql);
                        DatabaseClient.GenericExecuteSpec spec = dbClient.sql(sql.toString());
                        for (Map.Entry<String, Object> entry : params.entrySet()) {
                            spec = spec.bind(entry.getKey(), Parameter.fromOrEmpty(entry.getValue(), Object.class));
                        }
                        return spec.fetch().rowsUpdated().map(updated -> Map.of("rowsUpdated", updated));
                    }
                });
    }

    private String buildSimpleUpdateSQL(QueryTemplate template, Map<String, Integer> columnTypes, Map<String, Object> params) {
        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(template.getTableName()).append(" SET ");
        sql.append(buildSetClause(template, columnTypes, params));

        if (template.getConditions() != null && !template.getConditions().isEmpty()) {
            sql.append(" WHERE ").append(buildWhereClause(template, columnTypes, params));
        }

        return sql.toString();
    }

    private String buildSetClause(QueryTemplate template, Map<String, Integer> columnTypes, Map<String, Object> params) {
        StringJoiner setJoiner = new StringJoiner(", ");
        for (Map.Entry<String, Object> entry : template.getUpdatedValues().entrySet()) {
            String column = entry.getKey();
            Object value = entry.getValue();
            if (!columnTypes.containsKey(column.toLowerCase())) {
                throw new IllegalArgumentException("Invalid column in SET clause: " + column);
            }

            String paramKey = "set_" + column;
            setJoiner.add(column + " = :" + paramKey);
            params.put(paramKey, QueryParamCaster.cast(value, columnTypes.get(column.toLowerCase())));
        }
        return setJoiner.toString();
    }

    private String buildWhereClause(QueryTemplate template, Map<String, Integer> columnTypes, Map<String, Object> params) {
        StringJoiner whereJoiner = new StringJoiner(" ");
        for (QueryCondition condition : template.getConditions()) {
            String field = condition.getFieldName().toLowerCase();
            Object rawValue = condition.getValue();
            if (!columnTypes.containsKey(field)) continue;
            String paramKey = "where_" + field;
            params.put(paramKey, QueryParamCaster.cast(rawValue, columnTypes.get(field)));
            whereJoiner.add(field + " " + condition.getOperator().getSymbol() + " :" + paramKey);
            if (condition.getFilterOperator() != null) {
                whereJoiner.add(condition.getFilterOperator());
            }
        }
        return whereJoiner.toString();
    }

    private Mono<Object> emulateJoinUpdateForAllDBs(QueryTemplate template,
                                                    DatabaseConfig config,
                                                    DatabaseClient dbClient,
                                                    Map<String, Integer> columnTypes) {

        String primaryKey = template.getPrimaryKeyField() != null ? template.getPrimaryKeyField() : "id";
        String aliasMain = "t0";

        StringJoiner joinSql = new StringJoiner(" ");
        joinSql.add("SELECT").add(aliasMain + "." + primaryKey);
        joinSql.add("FROM").add(template.getTableName()).add(aliasMain);

        for (int i = 0; i < template.getJoins().size(); i++) {
            JoinConfig join = template.getJoins().get(i);
            String alias = join.getAlias() != null ? join.getAlias() : "j" + i;
            joinSql.add(join.getJoinType().name()).add("JOIN").add(join.getTable()).add(alias)
                    .add("ON").add(join.getOnCondition());
        }

        Map<String, Object> params = new HashMap<>();
        if (template.getConditions() != null && !template.getConditions().isEmpty()) {
            joinSql.add("WHERE").add(buildWhereClause(template, columnTypes, params));
        }

        String selectSql = joinSql.toString();
        log.debug("🕵️ Emulating JOIN via SELECT: {}", selectSql);

        DatabaseClient.GenericExecuteSpec spec = dbClient.sql(selectSql);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            spec = spec.bind(entry.getKey(), Parameter.fromOrEmpty(entry.getValue(), Object.class));
        }

        return spec.fetch().all()
                .map(row -> row.get(primaryKey))
                .collectList()
                .flatMap(ids -> {
                    if (ids.isEmpty()) return Mono.just(Map.of("rowsUpdated", 0));
                    return batchUpdateByIds(template.getTableName(), template, dbClient, columnTypes, ids, primaryKey)
                            .reduce(new AtomicInteger(0), (acc, count) -> {
                                acc.addAndGet(count);
                                return acc;
                            })
                            .map(acc -> Map.of("rowsUpdated", acc.get()));
                });
    }

    private Flux<Integer> batchUpdateByIds(String tableName,
                                           QueryTemplate template,
                                           DatabaseClient dbClient,
                                           Map<String, Integer> columnTypes,
                                           List<Object> allIds,
                                           String primaryKeyField) {

        List<List<Object>> batches = partitionList(allIds, BATCH_THRESHOLD);

        return Flux.fromIterable(batches)
                .filter(batch -> !batch.isEmpty())
                .flatMap(batch -> {
                    Map<String, Object> params = new HashMap<>();
                    String sql = "UPDATE " + tableName +
                            " SET " + buildSetClause(template, columnTypes, params) +
                            " WHERE " + primaryKeyField + " IN (:ids)";
                    params.put("ids", batch);

                    log.info("⚙️ Running UPDATE ({} rows): {}", batch.size(), sql);

                    DatabaseClient.GenericExecuteSpec spec = dbClient.sql(sql);
                    for (Map.Entry<String, Object> entry : params.entrySet()) {
                        spec = spec.bind(entry.getKey(), Parameter.fromOrEmpty(entry.getValue(), Object.class));
                    }

                    return spec.fetch().rowsUpdated().map(Long::intValue)
                            .doOnNext(updated -> log.info("✅ Batch Updated Rows: {}", updated));
                }, PARALLELISM);
    }

    private <T> List<List<T>> partitionList(List<T> list, int size) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            partitions.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return partitions;
    }
}
