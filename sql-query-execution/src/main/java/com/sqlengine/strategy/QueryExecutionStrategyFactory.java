package com.sqlengine.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class QueryExecutionStrategyFactory {

    private final ApplicationContext context;

    public QueryExecutionStrategy getStrategy(String type) {
        Map<String, QueryExecutionStrategy> all = context.getBeansOfType(QueryExecutionStrategy.class);
        return all.values().stream()
                .filter(s -> s.getType().equalsIgnoreCase(type))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No strategy found for query type: " + type));
    }
}
