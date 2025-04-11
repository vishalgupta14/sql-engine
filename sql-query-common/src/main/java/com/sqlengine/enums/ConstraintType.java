package com.sqlengine.enums;

/**
 * Enum representing the types of table-level constraints
 * that can be applied in a CREATE TABLE SQL statement.
 */
public enum ConstraintType {

    /**
     * Primary Key constraint.
     * Ensures values in the column(s) are unique and not null.
     */
    PRIMARY_KEY,

    /**
     * Foreign Key constraint.
     * Enforces referential integrity between two tables.
     */
    FOREIGN_KEY,

    /**
     * Unique constraint.
     * Ensures all values in the column(s) are distinct.
     */
    UNIQUE,

    /**
     * Check constraint.
     * Restricts the values that can be placed in a column using a boolean condition.
     */
    CHECK
}
