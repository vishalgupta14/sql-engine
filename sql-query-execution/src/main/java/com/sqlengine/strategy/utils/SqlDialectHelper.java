package com.sqlengine.strategy.utils;

import com.sqlengine.enums.DatabaseProvider;

public class SqlDialectHelper {

    public static String buildLimitOffsetClause(DatabaseProvider provider, Integer limit, Integer offset) {
        switch (provider) {
            case MYSQL:
            case POSTGRESQL:
            case MARIADB:
            case SQLITE:
                return buildLimitOffsetStandard(limit, offset);

            case ORACLE:
                return buildLimitOffsetOracle(limit, offset);

            case MSSQL:
                return buildLimitOffsetSqlServer(limit, offset);

            default:
                throw new UnsupportedOperationException("Unsupported provider: " + provider);
        }
    }

    private static String buildLimitOffsetStandard(Integer limit, Integer offset) {
        StringBuilder sb = new StringBuilder();
        if (limit != null) sb.append(" LIMIT ").append(limit);
        if (offset != null) sb.append(" OFFSET ").append(offset);
        return sb.toString();
    }

    private static String buildLimitOffsetOracle(Integer limit, Integer offset) {
        StringBuilder sb = new StringBuilder(" FETCH FIRST ");
        if (limit != null) {
            if (offset != null) {
                sb = new StringBuilder(" OFFSET ").append(offset).append(" ROWS FETCH NEXT ").append(limit).append(" ROWS ONLY");
            } else {
                sb.append(limit).append(" ROWS ONLY");
            }
        }
        return sb.toString();
    }

    private static String buildLimitOffsetSqlServer(Integer limit, Integer offset) {
        if (limit != null) {
            if (offset == null) offset = 0;
            return " OFFSET " + offset + " ROWS FETCH NEXT " + limit + " ROWS ONLY";
        }
        return "";
    }
}
