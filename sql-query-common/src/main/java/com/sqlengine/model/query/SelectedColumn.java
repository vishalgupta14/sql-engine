package com.sqlengine.model.query;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents a column to be selected in a SQL query.
 * <p>
 * This can be either:
 * <ul>
 *   <li>A simple column (e.g., <code>region</code>)</li>
 *   <li>An aggregate or derived expression (e.g., <code>SUM(amount)</code>)</li>
 * </ul>
 * Optionally, an alias can be provided for better readability or to use in ORDER BY or HAVING clauses.
 *
 * <h3>Examples</h3>
 *
 * <pre>{@code
 * // Simple column
 * SelectedColumn col1 = new SelectedColumn();
 * col1.setExpression("region");
 *
 * // Aggregate with alias
 * SelectedColumn col2 = new SelectedColumn();
 * col2.setExpression("SUM(amount)");
 * col2.setAlias("total_sales");
 * }</pre>
 *
 * <p>This structure is parsed and used in SELECT clause generation:
 *
 * <pre>
 *   SELECT region, SUM(amount) AS total_sales
 *   FROM orders
 *   GROUP BY region
 *   HAVING total_sales > 50000
 * </pre>
 *
 * @author Vishal
 */
@Getter
@Setter
public class SelectedColumn {

    /**
     * The column or expression to be selected.
     * Examples:
     * <ul>
     *   <li>"region"</li>
     *   <li>"SUM(amount)"</li>
     *   <li>"COUNT(DISTINCT user_id)"</li>
     * </ul>
     */
    private String expression;

    /**
     * Optional alias for the selected expression.
     * Useful for readability or for use in HAVING / ORDER BY clauses.
     * Examples: "total_sales", "user_count"
     */
    private String alias;
}
