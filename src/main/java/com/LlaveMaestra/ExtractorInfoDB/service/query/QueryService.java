package com.LlaveMaestra.ExtractorInfoDB.service.query;

import com.LlaveMaestra.ExtractorInfoDB.dto.PageData;
import com.LlaveMaestra.ExtractorInfoDB.service.security.SecurityValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class QueryService {

    private final JdbcTemplate jdbcTemplate;
    private final SecurityValidator securityValidator;

    /**
     * Obtiene el total de registros en una tabla.
     */
    public long countTableRows(String database, String schema, String table) {
        String safeName = securityValidator.getSafeTableName(database, schema, table);
        String sql = "SELECT COUNT(*) FROM " + safeName;
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }

    /**
     * Ejecuta una consulta segura y paginada para visualizar datos.
     */
    public PageData<Map<String, Object>> getTableData(String database, String schema, String table, int page, int size)
            throws SQLException {

        securityValidator.validateTableExistence(database, schema, table);

        long totalElements = countTableRows(database, schema, table);
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int offset = page * size;

        String safeName = securityValidator.getSafeTableName(database, schema, table);

        // Paginación profesional para SQL Server (2012+)
        String sql = String.format(
                "SELECT * FROM %s ORDER BY (SELECT NULL) OFFSET %d ROWS FETCH NEXT %d ROWS ONLY",
                safeName, offset, size);

        log.info("Visualizando datos (Página {}): {}", page, sql);
        List<Map<String, Object>> rawData = jdbcTemplate.queryForList(sql);

        List<Map<String, Object>> content = rawData.stream().map(row -> {
            Map<String, Object> lowerRow = new java.util.LinkedHashMap<>();
            row.forEach((k, v) -> lowerRow.put(k.toLowerCase(), v));
            return lowerRow;
        }).collect(Collectors.toList());

        return new PageData<>(content, totalElements, totalPages, page, size);
    }
}
