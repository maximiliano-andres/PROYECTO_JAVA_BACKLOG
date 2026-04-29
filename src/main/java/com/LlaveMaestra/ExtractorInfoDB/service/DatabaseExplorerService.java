package com.LlaveMaestra.ExtractorInfoDB.service;

import com.LlaveMaestra.ExtractorInfoDB.dto.ColumnInfo;
import com.LlaveMaestra.ExtractorInfoDB.dto.DatabaseInfo;
import com.LlaveMaestra.ExtractorInfoDB.dto.TableInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
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
public class DatabaseExplorerService {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseExplorerService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Obtiene la lista de bases de datos (catálogos) disponibles en el servidor.
     */
    public List<DatabaseInfo> getDatabases() throws SQLException {
        List<DatabaseInfo> databases = new ArrayList<>();
        try (Connection conn = jdbcTemplate.getDataSource().getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet rs = metaData.getCatalogs()) {
                while (rs.next()) {
                    databases.add(new DatabaseInfo(rs.getString("TABLE_CAT")));
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
            try (ResultSet rs = metaData.getTables(database, null, "%", new String[]{"TABLE", "VIEW"})) {
                while (rs.next()) {
                    tables.add(new TableInfo(
                            rs.getString("TABLE_NAME"),
                            rs.getString("TABLE_CAT"),
                            rs.getString("TABLE_SCHEM"),
                            rs.getString("TABLE_TYPE")
                    ));
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
                            rs.getInt("ORDINAL_POSITION")
                    ));
                }
            }
        }
        return columns;
    }

    /**
     * Ejecuta una consulta segura para visualizar datos de una tabla seleccionada.
     */
    public List<Map<String, Object>> getTableData(String database, String schema, String table, int limit) throws SQLException {
        // Validación de seguridad: Verificar que la tabla existe en los metadatos
        if (!tableExists(database, schema, table)) {
            log.error("Intento de acceso a tabla inexistente o no autorizada: {}.{}.{}", database, schema, table);
            throw new IllegalArgumentException("La tabla especificada no existe o no tiene permisos de acceso.");
        }

        // Construcción segura del nombre (escapando con corchetes para SQL Server)
        String safeName = String.format("[%s].[%s].[%s]", database, schema, table);
        
        // Limitar el número de filas para escalabilidad y rendimiento
        int safeLimit = Math.min(limit, 1000); 
        String sql = String.format("SELECT TOP (%d) * FROM %s", safeLimit, safeName);

        log.info("Visualizando datos: {}", sql);
        List<Map<String, Object>> rawData = jdbcTemplate.queryForList(sql);
        
        // Normalizar claves a minúsculas para coincidir con la lógica del frontend
        return rawData.stream().map(row -> {
            java.util.Map<String, Object> lowerRow = new java.util.LinkedHashMap<>();
            row.forEach((k, v) -> lowerRow.put(k.toLowerCase(), v));
            return lowerRow;
        }).collect(java.util.stream.Collectors.toList());
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
