package com.LlaveMaestra.ExtractorInfoDB.service;

import com.LlaveMaestra.ExtractorInfoDB.dto.ColumnInfo;
import com.LlaveMaestra.ExtractorInfoDB.dto.DatabaseInfo;
import com.LlaveMaestra.ExtractorInfoDB.dto.TableInfo;
import com.LlaveMaestra.ExtractorInfoDB.dto.PageData;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import com.LlaveMaestra.ExtractorInfoDB.config.AppProperties;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class DatabaseExplorerService {

    private final JdbcTemplate jdbcTemplate;
    private final AppProperties appProperties;


    /**
     * Obtiene la lista de bases de datos (catálogos) disponibles en el servidor.
     */
    public List<DatabaseInfo> getDatabases() throws SQLException {
        List<DatabaseInfo> databases = new ArrayList<>();
        try (Connection conn = jdbcTemplate.getDataSource().getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet rs = metaData.getCatalogs()) {
                while (rs.next()) {
                    String dbName = rs.getString("TABLE_CAT");

                    if (!appProperties.getExcludedDatabases().contains(dbName)) {
                        databases.add(new DatabaseInfo(dbName));
                    }
                }
            }
        }
        return databases;
    }

    /**
     * Obtiene las tablas de una base de datos específica.
     */
    public List<TableInfo> getTables(String database) throws SQLException {
        List<TableInfo> tables = new ArrayList<>();
        try (Connection conn = jdbcTemplate.getDataSource().getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            // null para schema y table name pattern para traer todo
            try (ResultSet rs = metaData.getTables(database, "dbo", "%", new String[] { "TABLE", "VIEW" })) {
                while (rs.next()) {
                    tables.add(new TableInfo(
                            rs.getString("TABLE_NAME"),
                            rs.getString("TABLE_CAT"),
                            rs.getString("TABLE_SCHEM"),
                            rs.getString("TABLE_TYPE")));
                }
            }
        }
        return tables;
    }

    /**
     * Obtiene las columnas de una tabla específica.
     */
    public List<ColumnInfo> getColumns(String database, String schema, String table) throws SQLException {
        List<ColumnInfo> columns = new ArrayList<>();
        try (Connection conn = jdbcTemplate.getDataSource().getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet rs = metaData.getColumns(database, schema, table, "%")) {
                while (rs.next()) {
                    columns.add(new ColumnInfo(
                            rs.getString("COLUMN_NAME"),
                            rs.getString("TYPE_NAME"),
                            rs.getInt("COLUMN_SIZE"),
                            "YES".equals(rs.getString("IS_NULLABLE")),
                            rs.getInt("ORDINAL_POSITION")));
                }
            }
        }
        return columns;
    }

    /**
     * Obtiene el total de registros en una tabla.
     */
    public long countTableRows(String database, String schema, String table) {
        String safeName = String.format("[%s].[%s].[%s]", database, schema, table);
        String sql = "SELECT COUNT(*) FROM " + safeName;
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }

    /**
     * Ejecuta una consulta segura y paginada para visualizar datos.
     */
    public PageData<Map<String, Object>> getTableData(String database, String schema, String table, int page, int size)
            throws SQLException {
        
        if (!tableExists(database, schema, table)) {
            log.error("Intento de acceso a tabla inexistente: {}.{}.{}", database, schema, table);
            throw new IllegalArgumentException("La tabla especificada no existe.");
        }

        long totalElements = countTableRows(database, schema, table);
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int offset = page * size;

        String safeName = String.format("[%s].[%s].[%s]", database, schema, table);
        
        // Paginación profesional para SQL Server (2012+)
        String sql = String.format(
            "SELECT * FROM %s ORDER BY (SELECT NULL) OFFSET %d ROWS FETCH NEXT %d ROWS ONLY",
            safeName, offset, size
        );

        log.info("Visualizando datos (Página {}): {}", page, sql);
        List<Map<String, Object>> rawData = jdbcTemplate.queryForList(sql);

        List<Map<String, Object>> content = rawData.stream().map(row -> {
            java.util.Map<String, Object> lowerRow = new java.util.LinkedHashMap<>();
            row.forEach((k, v) -> lowerRow.put(k.toLowerCase(), v));
            return lowerRow;
        }).collect(java.util.stream.Collectors.toList());

        return new PageData<>(content, totalElements, totalPages, page, size);
    }

    private boolean tableExists(String database, String schema, String table) throws SQLException {
        try (Connection conn = jdbcTemplate.getDataSource().getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet rs = metaData.getTables(database, schema, table, null)) {
                return rs.next();
            }
        }
    }
}
