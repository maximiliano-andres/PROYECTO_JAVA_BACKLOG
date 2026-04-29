package com.LlaveMaestra.ExtractorInfoDB.util;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

public class MapUtils {

    public static Object get(Map<String, Object> row, String... keys) {
        for (String key : keys) {
            // Busca la clave tal cual (SQL Server preserva el case del column label)
            if (row.containsKey(key)) {
                return row.get(key);
            }
            // Fallback: busca en minúsculas
            if (row.containsKey(key.toLowerCase())) {
                return row.get(key.toLowerCase());
            }
        }
        return null;
    }

    public static String getString(Map<String, Object> row, String... keys) {
        Object val = get(row, keys);
        return val != null ? val.toString() : null;
    }

    public static Integer getInt(Map<String, Object> row, String... keys) {
        Object val = get(row, keys);

        if (val instanceof Number) {
            return ((Number) val).intValue();
        }

        try {
            return val != null ? Integer.parseInt(val.toString()) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public static BigDecimal getBigDecimal(Map<String, Object> row, String... keys) {
        Object val = get(row, keys);

        if (val instanceof BigDecimal) {
            return (BigDecimal) val;
        }

        try {
            return val != null ? new BigDecimal(val.toString()) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public static LocalDate getLocalDate(Map<String, Object> row, String... keys) {
        Object val = get(row, keys);

        if (val instanceof Date) {
            return ((Date) val).toLocalDate();
        }

        return null;
    }

    public static OffsetDateTime getOffsetDateTime(Map<String, Object> row, String... keys) {
        Object val = get(row, keys);

        if (val == null) return null;

        if (val instanceof OffsetDateTime) {
            return (OffsetDateTime) val;
        }

        // JDBC devuelve Timestamp para columnas datetime/datetime2 de SQL Server
        if (val instanceof Timestamp) {
            return ((Timestamp) val).toInstant().atOffset(ZoneOffset.UTC);
        }

        if (val instanceof Date) {
            return ((Date) val).toLocalDate().atStartOfDay().atOffset(ZoneOffset.UTC);
        }

        return null;
    }
}