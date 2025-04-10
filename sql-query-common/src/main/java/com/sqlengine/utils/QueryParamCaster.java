package com.sqlengine.utils;

import java.math.BigDecimal;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class QueryParamCaster {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static Object cast(Object value, int sqlType) {
        if (value == null) return null;

        String strVal = value.toString().trim();

        try {
            switch (sqlType) {
                case Types.INTEGER:
                case Types.SMALLINT:
                case Types.TINYINT:
                    return Integer.parseInt(strVal);

                case Types.BIGINT:
                    return Long.parseLong(strVal);

                case Types.FLOAT:
                case Types.REAL:
                case Types.DOUBLE:
                    return Double.parseDouble(strVal);

                case Types.NUMERIC:
                case Types.DECIMAL:
                    return new BigDecimal(strVal);

                case Types.BOOLEAN:
                case Types.BIT:
                    return "1".equals(strVal) || "true".equalsIgnoreCase(strVal);

                case Types.DATE:
                    return LocalDate.parse(strVal, DATE_FORMAT);

                case Types.TIMESTAMP:
                case Types.TIMESTAMP_WITH_TIMEZONE:
                    return LocalDateTime.parse(strVal, TIMESTAMP_FORMAT);

                default:
                    return strVal; // fallback: string
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("❌ Failed to cast value: '" + strVal + "' for SQL type: " + sqlType, e);
        }
    }
}

