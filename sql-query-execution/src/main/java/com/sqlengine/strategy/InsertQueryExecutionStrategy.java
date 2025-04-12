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
        Map<String, Object> params = new HashMap<>();
        String sql = buildInsertSql(template, config.getProvider(), params);

        log.debug("🟢 Generated INSERT SQL: {}", sql);

        DatabaseClient.GenericExecuteSpec spec = dbClient.sql(sql);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            spec = spec.bind(entry.getKey(), Parameter.fromOrEmpty(entry.getValue(), Object.class));
        }

        // Handle RETURNING fallback
        if (template.getReturningFields() != null && !template.getReturningFields().isEmpty()) {
            return spec.fetch().all().collectList().map(list -> Map.of("returning", list));
        }

        return spec.fetch().rowsUpdated().map(updated -> Map.of("rowsInserted", updated));
    }

    private String buildInsertSql(QueryTemplate template, DatabaseProvider provider, Map<String, Object> params) {
        StringBuilder sql = new StringBuilder();

        if (Boolean.TRUE.equals(template.isUseReplace()) && supportsReplace(provider)) {
            sql.append("REPLACE INTO ");
        } else if (Boolean.TRUE.equals(template.isUseMerge())) {
            throw new UnsupportedOperationException("MERGE INSERT not implemented yet");
        } else {
            sql.append("INSERT INTO ");
        }

        sql.append(template.getTableName());

        if (Boolean.TRUE.equals(template.isInsertFromSelect())) {
            sql.append(" (").append(String.join(", ", template.getInsertColumns())).append(") ");
            sql.append(template.getSubqueries().get(0).getQuery()); // Assumes one SELECT subquery
        } else {
            List<String> columns = new ArrayList<>();
            List<String> valuePlaceholders = new ArrayList<>();

            for (Map.Entry<String, Object> entry : template.getInsertValues().entrySet()) {
                String col = entry.getKey();
                String paramKey = "val_" + col;
                columns.add(col);
                valuePlaceholders.add(":" + paramKey);
                params.put(paramKey, entry.getValue());
            }

            sql.append(" (").append(String.join(", ", columns)).append(") ");
            sql.append("VALUES (").append(String.join(", ", valuePlaceholders)).append(")");
        }

        // Upsert / conflict resolution
        if (template.getConflictColumns() != null && !template.getConflictColumns().isEmpty()) {
            switch (provider) {
                case POSTGRESQL -> {
                    sql.append(" ON CONFLICT (")
                            .append(String.join(", ", template.getConflictColumns()))
                            .append(")");

                    if (template.getUpsertValues() != null && !template.getUpsertValues().isEmpty()) {
                        sql.append(" DO UPDATE SET ");
                        List<String> updates = new ArrayList<>();
                        for (Map.Entry<String, Object> e : template.getUpsertValues().entrySet()) {
                            String col = e.getKey();
                            String paramKey = "upsert_" + col;
                            updates.add(col + " = :" + paramKey);
                            params.put(paramKey, e.getValue());
                        }
                        sql.append(String.join(", ", updates));
                    } else {
                        sql.append(" DO NOTHING");
                    }
                }
                case MYSQL, MARIADB -> {
                    if (template.getUpsertValues() != null && !template.getUpsertValues().isEmpty()) {
                        sql.append(" ON DUPLICATE KEY UPDATE ");
                        List<String> updates = new ArrayList<>();
                        for (Map.Entry<String, Object> e : template.getUpsertValues().entrySet()) {
                            String col = e.getKey();
                            String paramKey = "upsert_" + col;
                            updates.add(col + " = :" + paramKey);
                            params.put(paramKey, e.getValue());
                        }
                        sql.append(String.join(", ", updates));
                    }
                }
            }
        }

        // RETURNING support
        if (template.getReturningFields() != null && !template.getReturningFields().isEmpty()) {
            if (supportsReturning(provider)) {
                sql.append(" RETURNING ").append(String.join(", ", template.getReturningFields()));
            }
        }

        return sql.toString();
    }

    private boolean supportsReturning(DatabaseProvider provider) {
        return EnumSet.of(DatabaseProvider.POSTGRESQL, DatabaseProvider.ORACLE, DatabaseProvider.MSSQL).contains(provider);
    }

    private boolean supportsReplace(DatabaseProvider provider) {
        return EnumSet.of(DatabaseProvider.MYSQL, DatabaseProvider.MARIADB, DatabaseProvider.SQLITE).contains(provider);
    }
}
