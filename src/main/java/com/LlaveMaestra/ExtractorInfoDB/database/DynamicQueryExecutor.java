package com.LlaveMaestra.ExtractorInfoDB.database;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSetMetaData;
import java.util.*;

import lombok.extern.slf4j.Slf4j;

/**
 * Clase que permite ejecutar consultas dinámicas a la base de datos.
 * Se utiliza para ejecutar consultas que no están predefinidas en el código.
 * <p>
 * Principio: I (Interface Segregation) + D (Dependency Inversion).
 */
@Repository
@Slf4j
public class DynamicQueryExecutor {

    private final JdbcTemplate jdbcTemplate;

    public DynamicQueryExecutor(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> execute(String sql) {

        // Muestra servidor, base de datos activa y SQL antes de ejecutar
        jdbcTemplate.execute((java.sql.Connection conn) -> {
            log.info("╔══════════════════════════════════════════════════════");
            log.info("║ SERVIDOR : {}", conn.getMetaData().getURL());
            log.info("║ BASE DE DATOS : {}", conn.getCatalog());
            log.info("║ SQL      : {}", sql);
            log.info("╚══════════════════════════════════════════════════════");
            return null;
        });

        return jdbcTemplate.query(sql, rs -> {
            List<Map<String, Object>> results = new ArrayList<>();

            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();

                for (int i = 1; i <= columnCount; i++) {
                    String columnName = meta.getColumnLabel(i);
                    Object value = rs.getObject(i);

                    row.put(columnName.toLowerCase(), value);
                }

                results.add(row);
            }

            return results;
        });
    }
}