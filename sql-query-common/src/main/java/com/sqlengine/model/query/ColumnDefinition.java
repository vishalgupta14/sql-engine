package com.sqlengine.model.query;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents a single column definition within a CREATE TABLE statement.
 * This model captures the column's name, data type, constraints, default values, and optional comments.
 */
@Getter
@Setter
public class ColumnDefinition {

    /**
     * Name of the column.
     */
    private String name;

    /**
     * SQL data type of the column, including length/precision if applicable.
     * Example: "VARCHAR(255)", "INT", "DECIMAL(10,2)", "BOOLEAN"
     */
    private String dataType;

    /**
     * Indicates whether the column can contain NULL values.
     * If false, `NOT NULL` will be added to the definition.
     */
    private boolean nullable = true;

    /**
     * Indicates whether this column is a primary key.
     * Can be used in addition to or instead of a table-level constraint.
     */
    private boolean primaryKey = false;

    /**
     * Indicates whether the column must be unique.
     * Adds `UNIQUE` constraint at the column level.
     */
    private boolean unique = false;

    /**
     * If true, enables auto-increment or serial behavior.
     * Interpretation may vary by database (e.g., AUTO_INCREMENT in MySQL, SERIAL in PostgreSQL).
     */
    private boolean autoIncrement = false;

    /**
     * Default value for the column, if any.
     * Can be a literal value or function (e.g., 'CURRENT_TIMESTAMP').
     */
    private Object defaultValue;

    /**
     * Optional comment describing the column.
     * Supported in some databases like MySQL for documentation purposes.
     */
    private String comment;
}
