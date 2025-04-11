package com.sqlengine.model.query;

import com.sqlengine.enums.SortDirection;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * Represents the structure and metadata required to dynamically build a SELECT SQL query.
 * This block is part of a QueryTemplate and is only used when `queryType` is 'SELECT'.
 * Supports advanced SQL features like joins, subqueries, CTEs, grouping, ordering, and pagination.
 */
@Getter
@Setter
public class SelectQueryBlock {

    /**
     * List of Common Table Expressions (CTEs) to be prefixed with the SELECT query using `WITH` clause.
     */
    private List<CteBlock> ctes;

    /**
     * Name of the main table on which the SELECT query will be performed.
     */
    private String tableName;

    /**
     * List of UNION or UNION ALL subqueries that follow the main SELECT block.
     */
    private List<UnionQuery> unions;

    /**
     * List of JOIN clauses to combine other tables with the main table.
     */
    private List<JoinConfig> joins;

    /**
     * List of columns to select in the final output of the query.
     */
    private List<SelectedColumn> selectedColumns;

    /**
     * If true, adds `DISTINCT` to the SELECT clause to return unique rows.
     */
    private boolean distinct = false;

    /**
     * Conditions to be applied in the WHERE clause of the query.
     */
    private List<QueryCondition> conditions;

    /**
     * Conditions to be applied in the HAVING clause, used after GROUP BY.
     */
    private List<QueryCondition> havingConditions;

    /**
     * List of subqueries used inside the SELECT statement, typically in the SELECT columns.
     */
    private List<SubqueryBlock> subqueries;

    /**
     * Map specifying how to sort the result set.
     * Key = column name, Value = ASC or DESC.
     * Example: {"created_at": DESC, "name": ASC}
     */
    private Map<String, SortDirection> orderBy;

    /**
     * Maximum number of rows to return (LIMIT clause).
     */
    private Integer limit;

    /**
     * Number of rows to skip from the beginning (OFFSET clause), useful for pagination.
     */
    private Integer offset;

    /**
     * List of column names used in the GROUP BY clause.
     */
    private List<String> groupBy;
}
