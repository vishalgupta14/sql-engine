package com.sqlengine.strategy;

import com.sqlengine.model.DatabaseConfig;
import com.sqlengine.model.QueryTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;

public interface QueryExecutionStrategy {
    String getType();
    Mono<Object> execute(QueryTemplate template, DatabaseConfig config, DatabaseClient dbClient);
}
