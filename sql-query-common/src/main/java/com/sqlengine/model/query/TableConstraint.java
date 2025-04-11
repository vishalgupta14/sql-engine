package com.sqlengine.model.query;

import com.sqlengine.enums.ConstraintType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Represents a table-level constraint to be applied in a `CREATE TABLE` SQL statement.
 * This includes constraints like PRIMARY KEY, FOREIGN KEY, UNIQUE, and CHECK.
 * It complements the column-level definitions by enabling composite keys and relational integrity.
 */
@Getter
@Setter
public class TableConstraint {

    /**
     * Optional name for the constraint.
     * If provided, it will be used in the SQL as: CONSTRAINT constraint_name ...
     */
    private String name;

    /**
     * Type of constraint (e.g., PRIMARY_KEY, FOREIGN_KEY, UNIQUE, CHECK).
     */
    private ConstraintType type;

    /**
     * List of column names the constraint applies to.
     * Used for PRIMARY KEY, UNIQUE, FOREIGN KEY, and CHECK conditions involving columns.
     */
    private List<String> columns;

    /**
     * Referenced table for FOREIGN KEY constraints.
     * Required only if type is FOREIGN_KEY.
     */
    private String referenceTable;

    /**
     * List of referenced columns in the foreign table.
     * Must align with the `columns` list in order and count.
     */
    private List<String> referenceColumns;

    /**
     * SQL expression used in CHECK constraints.
     * Required only if type is CHECK.
     * Example: "age >= 18"
     */
    private String checkCondition;
}
