package com.orchedule.shared.jdbc;

import java.util.Map;

/** Explicit handling for required SQL numeric columns; never invents a default. */
public final class JdbcValues {
    private JdbcValues() {}

    public static int requiredInt(Map<String, ?> row, String column) {
        Object value = row.get(column);
        if (!(value instanceof Number number)) {
            throw new IllegalStateException(
                "Expected a non-null SQL number in column '" + column
                    + "', got: " + (value == null ? "null" : value.getClass().getName()));
        }
        return number.intValue();
    }

    public static long requiredLong(Map<String, ?> row, String column) {
        Object value = row.get(column);
        if (!(value instanceof Number number)) {
            throw new IllegalStateException(
                "Expected a non-null SQL number in column '" + column
                    + "', got: " + (value == null ? "null" : value.getClass().getName()));
        }
        return number.longValue();
    }
}

