package com.sqlengine.strategy;

import com.sqlengine.enums.DatabaseProvider;
import com.sqlengine.manager.TableMetadataManager;
import com.sqlengine.model.DatabaseConfig;
import com.sqlengine.model.QueryTemplate;
import com.sqlengine.utils.QueryParamCaster;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.Parameter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component("insert")
@RequiredArgsConstructor
public class InsertQueryExecutionStrategy implements QueryExecutionStrategy {

    private final TableMetadataManager tableMetadataManager;

    @Override
    public String getType() {
        return "INSERT";
    }

    @Override
    public Mono<Object> execute(QueryTemplate template, DatabaseConfig config, DatabaseClient dbClient) {
        if (template.getInsertValues() == null || template.getInsertValues().isEmpty()) {
            return Mono.error(new IllegalArgumentException("❌ insertValues are required for INSERT query"));
        }

        String table = template.getTableName();
        Map<String, Object> insertValues = template.getInsertValues();
        List<String> returningFields = template.getReturningFields();

        StringJoiner columnsJoiner = new StringJoiner(", ");
        StringJoiner valuesJoiner = new StringJoiner(", ");
        Map<String, Object> params = new HashMap<>();

        insertValues.forEach((col, val) -> {
            columnsJoiner.add(col);
            String paramKey = "val_" + col;
            valuesJoiner.add(":" + paramKey);
            params.put(paramKey, val);
        });

        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ").append(table)
                .append(" (").append(columnsJoiner).append(") ")
                .append("VALUES (").append(valuesJoiner).append(")");

        // ✅ DB-native RETURNING support
        if (returningFields != null && !returningFields.isEmpty()) {
            if (supportsReturning(config.getProvider())) {
                sql.append(" RETURNING ").append(String.join(", ", returningFields));
            }
        }

        log.debug("🟢 Generated INSERT SQL: {}", sql);

        DatabaseClient.GenericExecuteSpec spec = dbClient.sql(sql.toString());
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            spec = spec.bind(entry.getKey(), Parameter.fromOrEmpty(entry.getValue(), Object.class));
        }

        return spec.fetch().all().collectList().flatMap(result -> {
            if (!result.isEmpty()) {
                return Mono.just(result);
            }

            // 🔁 Emulate RETURNING logic
            if (returningFields != null && !returningFields.isEmpty() && !supportsReturning(config.getProvider())) {
                log.info("ℹ️ Emulating RETURNING clause...");
                return emulateReturningAfterInsert(template, config, dbClient);
            }

            return Mono.just(Map.of("inserted", 1));
        });
    }

    private Mono<Object> emulateReturningAfterInsert(QueryTemplate template,
                                                     DatabaseConfig config,
                                                     DatabaseClient dbClient) {

        String table = template.getTableName();
        Map<String, Object> insertValues = template.getInsertValues();
        List<String> returningFields = template.getReturningFields();

        return tableMetadataManager.getColumnTypesReactive(config, dbClient, table)
                .flatMap(columnTypes -> {
                    StringJoiner whereJoiner = new StringJoiner(" AND ");
                    Map<String, Object> params = new HashMap<>();

                    insertValues.forEach((col, val) -> {
                        if (columnTypes.containsKey(col.toLowerCase())) {
                            Object casted = QueryParamCaster.cast(val, columnTypes.get(col.toLowerCase()));
                            String key = "w_" + col;
                            whereJoiner.add(col + " = :" + key);
                            params.put(key, casted);
                        }
                    });

                    String sql = "SELECT " + String.join(", ", returningFields)
                            + " FROM " + table + " WHERE " + whereJoiner;

                    log.debug("🧩 Emulated RETURNING SQL: {}", sql);

                    DatabaseClient.GenericExecuteSpec spec = dbClient.sql(sql);
                    for (Map.Entry<String, Object> entry : params.entrySet()) {
                        spec = spec.bind(entry.getKey(), Parameter.fromOrEmpty(entry.getValue(), Object.class));
                    }

                    return spec.fetch().all().collectList().cast(Object.class);
                });
    }

    private boolean supportsReturning(DatabaseProvider provider) {
        return provider == DatabaseProvider.POSTGRESQL
                || provider == DatabaseProvider.ORACLE
                || provider == DatabaseProvider.MSSQL;
    }
}
