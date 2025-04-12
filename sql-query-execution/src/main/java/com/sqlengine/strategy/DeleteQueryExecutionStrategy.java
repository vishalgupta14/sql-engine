package com.sqlengine.strategy;

import com.sqlengine.enums.DatabaseProvider;
import com.sqlengine.enums.JoinType;
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
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

@Slf4j
@Component("delete")
@RequiredArgsConstructor
public class DeleteQueryExecutionStrategy implements QueryExecutionStrategy {

    private final TableMetadataManager tableMetadataManager;

    private static final Set<DatabaseProvider> NATIVE_JOIN_DELETE_SUPPORTED = Set.of(
            DatabaseProvider.MYSQL, DatabaseProvider.MARIADB, DatabaseProvider.MSSQL
    );

    @Override
    public String getType() {
        return "DELETE";
    }

    @Override
    public Mono<Object> execute(QueryTemplate template, DatabaseConfig config, DatabaseClient dbClient) {
        return tableMetadataManager.getColumnTypesReactive(config, dbClient, template.getTableName())
                .flatMap(columnTypes -> {
                    if (template.getSqlQuery() != null && !template.getSqlQuery().isBlank()) {
                        return bindAndReturnResults(dbClient, template.getSqlQuery(), new HashMap<>())
                                .collectList().cast(Object.class);
                    }

                    if (template.getJoins() != null && !template.getJoins().isEmpty()) {
                        if (NATIVE_JOIN_DELETE_SUPPORTED.contains(config.getProvider())) {
                            return nativeDeleteWithJoin(template, config, dbClient, columnTypes).cast(Object.class);
                        } else {
                            return emulateDeleteWithJoin(template, config, dbClient, columnTypes).cast(Object.class);
                        }
                    } else {
                        return simpleDelete(template, config, dbClient, columnTypes).cast(Object.class);
                    }
                });
    }

    private Mono<Flux<Map<String, Object>>> nativeDeleteWithJoin(QueryTemplate template,
                                                                 DatabaseConfig config,
                                                                 DatabaseClient dbClient,
                                                                 Map<String, Integer> columnTypes) {
        Map<String, Object> params = new HashMap<>();
        StringBuilder sql = new StringBuilder("DELETE ").append(template.getTableName()).append(" FROM ")
                .append(template.getTableName());

        for (JoinConfig join : template.getJoins()) {
            sql.append(" ").append(join.getJoinType().name()).append(" JOIN ")
                    .append(join.getTable());
            if (StringUtils.hasText(join.getAlias())) {
                sql.append(" ").append(join.getAlias());
            }
            sql.append(" ON ").append(join.getOnCondition());
        }

        if (template.getConditions() != null && !template.getConditions().isEmpty()) {
            sql.append(" WHERE ").append(buildWhereClause(template, columnTypes, params));
        }

        if (template.getReturningFields() != null && !template.getReturningFields().isEmpty()
                && supportsReturning(config.getProvider())) {
            sql.append(" RETURNING ").append(String.join(", ", template.getReturningFields()));
        }

        log.debug("🧨 Native DELETE WITH JOIN SQL: {}", sql);
        return Mono.just(bindAndReturnResults(dbClient, sql.toString(), params));
    }

    private Mono<Flux<Map<String, Object>>> simpleDelete(QueryTemplate template,
                                                         DatabaseConfig config,
                                                         DatabaseClient dbClient,
                                                         Map<String, Integer> columnTypes) {
        Map<String, Object> params = new HashMap<>();
        StringBuilder sql = new StringBuilder("DELETE FROM ").append(template.getTableName());

        if (template.getConditions() != null && !template.getConditions().isEmpty()) {
            sql.append(" WHERE ").append(buildWhereClause(template, columnTypes, params));
        }

        if (template.getReturningFields() != null && !template.getReturningFields().isEmpty()
                && supportsReturning(config.getProvider())) {
            sql.append(" RETURNING ").append(String.join(", ", template.getReturningFields()));
        }

        log.debug("🧹 Simple DELETE SQL: {}", sql);
        return Mono.just(bindAndReturnResults(dbClient, sql.toString(), params));
    }

    private Mono<Flux<Map<String, Object>>> emulateDeleteWithJoin(QueryTemplate template,
                                                                  DatabaseConfig config,
                                                                  DatabaseClient dbClient,
                                                                  Map<String, Integer> columnTypes) {

        String primaryKey = template.getPrimaryKeyField() != null ? template.getPrimaryKeyField() : "id";
        String alias = "t0";
        Map<String, Object> params = new HashMap<>();

        StringBuilder selectSql = new StringBuilder("SELECT ").append(alias).append(".").append(primaryKey)
                .append(" FROM ").append(template.getTableName()).append(" ").append(alias);

        for (int i = 0; i < template.getJoins().size(); i++) {
            JoinConfig join = template.getJoins().get(i);
            String jAlias = join.getAlias() != null ? join.getAlias() : "j" + i;
            selectSql.append(" ").append(join.getJoinType().name()).append(" JOIN ")
                    .append(join.getTable()).append(" ").append(jAlias)
                    .append(" ON ").append(join.getOnCondition());
        }

        if (template.getConditions() != null && !template.getConditions().isEmpty()) {
            selectSql.append(" WHERE ").append(buildWhereClause(template, columnTypes, params));
        }

        String idSelectionSql = selectSql.toString();
        log.debug("📌 Emulated DELETE ID query: {}", idSelectionSql);

        DatabaseClient.GenericExecuteSpec spec = dbClient.sql(idSelectionSql);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            spec = spec.bind(entry.getKey(), Parameter.fromOrEmpty(entry.getValue(), Object.class));
        }

        return spec.fetch().all()
                .map(row -> row.get(primaryKey))
                .collectList()
                .flatMap(ids -> {
                    if (ids.isEmpty()) return Mono.just(Flux.just(Map.of("rowsDeleted", 0)));

                    StringBuilder deleteSql = new StringBuilder("DELETE FROM ")
                            .append(template.getTableName())
                            .append(" WHERE ").append(primaryKey).append(" IN (:ids)");

                    log.debug("🔁 Emulated DELETE using ID list: {}", deleteSql);

                    Map<String, Object> deleteParams = new HashMap<>();
                    deleteParams.put("ids", ids);

                    if (template.getReturningFields() != null && !template.getReturningFields().isEmpty()
                            && supportsReturning(config.getProvider())) {
                        deleteSql.append(" RETURNING ").append(String.join(", ", template.getReturningFields()));
                    }

                    return Mono.just(bindAndReturnResults(dbClient, deleteSql.toString(), deleteParams));
                });
    }

    private boolean supportsReturning(DatabaseProvider provider) {
        return provider == DatabaseProvider.POSTGRESQL
                || provider == DatabaseProvider.ORACLE
                || provider == DatabaseProvider.MSSQL;
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

    private Flux<Map<String, Object>> bindAndReturnResults(DatabaseClient dbClient, String sql, Map<String, Object> params) {
        DatabaseClient.GenericExecuteSpec spec = dbClient.sql(sql);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            spec = spec.bind(entry.getKey(), Parameter.fromOrEmpty(entry.getValue(), Object.class));
        }
        return spec.fetch().all();
    }
}
