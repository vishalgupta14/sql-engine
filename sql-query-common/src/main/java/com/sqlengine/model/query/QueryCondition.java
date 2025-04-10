package com.sqlengine.model.query;

import com.sqlengine.enums.QueryOperator;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a single filter condition used in SQL query templates.
 */
@Getter
@Setter
public class QueryCondition {

    /**
     * The column name in the database table to filter on.
     */
    private String fieldName;

    /**
     * The value to filter with.
     *
     * <p><b>For DATE fields:</b> format should be {@code yyyy-MM-dd}, e.g., {@code "2024-04-09"}.</p>
     * <p><b>For TIMESTAMP fields:</b> format should be {@code yyyy-MM-dd'T'HH:mm:ss}, e.g., {@code "2024-04-09T14:30:00"}.</p>
     * <p><b>For BOOLEAN fields:</b> use {@code "true"}, {@code "false"}, or {@code "1"}, {@code "0"}.</p>
     */
    private String value;

    /**
     * The comparison operator to use.
     * <p>Examples: {@code EQUALS}, {@code GREATER_THAN}, {@code LESS_THAN}, {@code LIKE}, {@code IN}</p>
     */
    private QueryOperator operator;

    /**
     * Logical operator to chain with the next condition.
     * <p>Options: {@code AND}, {@code OR}</p>
     */
    private String filterOperator;
}
