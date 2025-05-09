package com.sqlengine.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sqlengine.enums.SortDirection;
import com.sqlengine.model.query.CteBlock;
import com.sqlengine.model.query.JoinConfig;
import com.sqlengine.model.query.QueryCondition;
import com.sqlengine.model.query.SelectedColumn;
import com.sqlengine.model.query.SubqueryBlock;
import com.sqlengine.model.query.UnionQuery;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Represents a metadata-driven SQL query template stored in MongoDB.
 * This model supports advanced SQL features such as CTEs, joins, subqueries,
 * group by, order by, and unions. Designed for dynamic and reusable query generation.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Document("query_templates")
public class QueryTemplate {

    /** Unique identifier for the query template. */
    @Id
    private String id;

    /** Human-readable name for this query template. */
    private String templateName;

    /** Username or identifier of the user who created the template. */
    private String createdBy;

    /** Type of SQL query: SELECT, INSERT, UPDATE, or DELETE. */
    private String queryType;

    /** List of Common Table Expressions (CTEs) used in the query. */
    private List<CteBlock> ctes;

    /** Main table name that the query is executed on. */
    private String tableName;

    /** List of UNION or UNION ALL queries that follow the main query. */
    private List<UnionQuery> unions;

    /** List of JOIN configurations for combining tables. */
    private List<JoinConfig> joins;

    /** List of columns to be selected in the result. */
    private List<SelectedColumn> selectedColumns;

    /** Whether to select distinct records. */
    private boolean distinct = false;

    /**
     * Key-value map representing columns to be updated and their values.
     * Example: {"status": "COMPLETED", "updated_at": "now()"}
     */
    private Map<String, Object> updatedValues;

    /** Optional primary key field (defaults to 'id') */
    private String primaryKeyField = "id";

    /** Conditions for the WHERE clause. */
    private List<QueryCondition> conditions;

    /** Conditions for the HAVING clause. */
    private List<QueryCondition> havingConditions;

    /** Subqueries embedded within the main query logic. */
    private List<SubqueryBlock> subqueries;

    /**
     * Sorting configuration.
     * Key = column name, Value = sort direction (ASC/DESC).
     * Example: {"created_at": DESC, "name": ASC}
     */
    private Map<String, SortDirection> orderBy;

    /** Limit on the number of rows returned. */
    private Integer limit;

    /** Number of rows to skip (for pagination). */
    private Integer offset;

    /** List of columns to group the results by. */
    private List<String> groupBy;

    private List<String> returningFields; // e.g., ["id", "email", "status"]

    /** Timestamp when the query template was created. */
    private LocalDateTime createdAt;

    /** Timestamp when the query template was last updated. */
    private LocalDateTime updatedAt;

    // --- INSERT specific fields ---

    /** Key-value pairs for single row insert. */
    private Map<String, Object> insertValues;

    /** If true, use INSERT INTO ... SELECT ... instead of VALUES */
    private boolean insertFromSelect;

    /** Columns to insert into (used with insertFromSelect + subqueries) */
    private List<String> insertColumns;

    /** Conflict resolution: unique keys for ON CONFLICT (PostgreSQL) */
    private List<String> conflictColumns;

    /** Columns and values to update on conflict (Upsert behavior) */
    private Map<String, Object> upsertValues;

    /** Use REPLACE INTO / INSERT OR REPLACE (MySQL/MariaDB/SQLite) */
    private boolean useReplace;

    /** Use MERGE INTO (Oracle, SQL Server, MariaDB 10.3+) */
    private boolean useMerge;

    /** Raw SQL query to execute directly if set. */
    private String sqlQuery;

    // --- DELETE-specific fields ---

    /** Flag to indicate DELETE query with JOIN support */
    private boolean deleteWithJoin;

    /** Optional alias for target table in DELETE ... JOIN */
    private String deleteTableAlias;

    private String ddlStatement; // e.g., "CREATE TABLE users (id INT PRIMARY KEY, name VARCHAR(255))"

}
