package com.sqlengine.model.query;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Represents the structure and metadata required to dynamically generate a `CREATE TABLE` SQL statement.
 * This block is part of a `QueryTemplate` and is only used when `queryType` is 'CREATE'.
 * Supports various column definitions, table constraints, and table options like temporary status or comments.
 */
@Getter
@Setter
public class CreateTableBlock {

    /**
     * If true, generates `CREATE TABLE IF NOT EXISTS` to avoid errors if the table already exists.
     */
    private boolean ifNotExists;

    /**
     * If true, creates a temporary table which exists only during the database session.
     */
    private boolean temporary;

    /**
     * Name of the table to be created.
     */
    private String tableName;

    /**
     * Optional comment or description for the table.
     * Some databases (like MySQL) support table-level comments.
     */
    private String tableComment;

    /**
     * List of column definitions including name, type, constraints, and default values.
     */
    private List<ColumnDefinition> columns;

    /**
     * List of table-level constraints such as primary keys, foreign keys, unique constraints, and checks.
     */
    private List<TableConstraint> constraints;
}
