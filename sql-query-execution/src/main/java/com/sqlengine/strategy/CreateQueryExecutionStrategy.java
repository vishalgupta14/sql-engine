package com.sqlengine.strategy;

import com.sqlengine.model.DatabaseConfig;
import com.sqlengine.model.QueryTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component("create")
@RequiredArgsConstructor
public class CreateQueryExecutionStrategy implements QueryExecutionStrategy {
    @Override
    public String getType() {
        return "CREATE";
    }

    @Override
    public Mono<Object> execute(QueryTemplate template, DatabaseConfig config, DatabaseClient dbClient) {
        if (template.getDdlStatement() == null || template.getDdlStatement().isBlank()) {
            return Mono.error(new IllegalArgumentException("DDL statement cannot be empty for CREATE operations."));
        }

        return dbClient.sql(template.getDdlStatement())
                .fetch().rowsUpdated()
                .map(count -> Map.of("ddlExecuted", true));
    }
}
