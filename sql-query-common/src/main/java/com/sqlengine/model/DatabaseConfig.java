package com.sqlengine.model;

import com.sqlengine.enums.DatabaseProvider;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Represents the configuration details required to connect to an external database.
 * This configuration supports multiple providers such as MySQL, PostgreSQL, Oracle, and MSSQL.
 * It also includes support for fallback configurations and privacy-aware configurations.
 *
 * <p><b>Example (MySQL):</b></p>
 * <pre>{@code
 * {
 *   "databaseConnectionName": "client-prod-db",
 *   "provider": "MYSQL",
 *   "config": {
 *     "host": "localhost",
 *     "port": 3306,
 *     "database": "analytics_db",
 *     "username": "dbuser",
 *     "password": "encrypted-password"
 *   },
 *   "isActive": true
 * }
 * }</pre>
 */
@Getter
@Setter
@Document("database_config")
public class DatabaseConfig {

    /** Unique identifier for this database configuration. */
    @Id
    private String id;

    /** Friendly name or alias for this database connection. */
    private String databaseConnectionName;

    /** Type of database provider (e.g., MYSQL, POSTGRESQL, ORACLE, MSSQL). */
    private DatabaseProvider provider;

    /**
     * Connection configuration parameters for the database.
     * Example keys for MySQL: host, port, database, username, password.
     */
    private Map<String, Object> config;

    /** Flag indicating if this configuration is active and should be used. */
    private boolean isActive;

    /**
     * Reference to another DatabaseConfig ID to use as fallback
     * when this config fails or is unreachable.
     */
    private String fallbackConfigId;

    /**
     * Optional privacy-compliant fallback config to be used in regulated scenarios.
     * Can contain redacted credentials or alternate endpoints.
     */
    private Map<String, Object> privacyFallbackConfig;

    /** Timestamp of when the configuration was created. */
    private LocalDateTime createdAt;

    /** Timestamp of the last update to this configuration. */
    private LocalDateTime updatedAt;
}
