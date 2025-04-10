package com.sqlengine.enums;

/**
 * Enum representing supported database providers.
 */
public enum DatabaseProvider {
    MYSQL,
    POSTGRESQL,
    ORACLE,
    MSSQL,
    MARIADB,
    SQLITE;

    public static DatabaseProvider fromString(String name) {
        for (DatabaseProvider provider : values()) {
            if (provider.name().equalsIgnoreCase(name)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("Unsupported database provider: " + name);
    }
}
