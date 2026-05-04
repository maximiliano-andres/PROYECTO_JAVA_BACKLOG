package com.LlaveMaestra.ExtractorInfoDB.service.metadata;

import com.LlaveMaestra.ExtractorInfoDB.config.AppProperties;
import com.LlaveMaestra.ExtractorInfoDB.dto.ColumnInfo;
import com.LlaveMaestra.ExtractorInfoDB.dto.DatabaseInfo;
import com.LlaveMaestra.ExtractorInfoDB.dto.TableInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MetadataService {

    private final JdbcTemplate jdbcTemplate;
    private final AppProperties appProperties;

    /**
     * Obtiene la lista de bases de datos (catálogos) disponibles en el servidor.
     */
    @Cacheable("databases")
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
     * Obtiene las tablas de una base de datos y esquema específicos.
     * Si el esquema es nulo, intenta detectarlo automáticamente.
     */
    @Cacheable(value = "tables", key = "#database + '-' + #schema")
    public List<TableInfo> getTables(String database, String schema) throws SQLException {
        List<TableInfo> tables = new ArrayList<>();
        try (Connection conn = jdbcTemplate.getDataSource().getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();

            String schemaPattern = resolveSchema(schema, conn);
            log.info("Buscando tablas en Catálogo: [{}], Esquema: [{}]", database, schemaPattern);

            try (ResultSet rs = metaData.getTables(database, schemaPattern, "%", new String[] { "TABLE", "VIEW" })) {
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
    @Cacheable(value = "columns", key = "#database + '-' + #schema + '-' + #table")
    public List<ColumnInfo> getColumns(String database, String schema, String table) throws SQLException {
        List<ColumnInfo> columns = new ArrayList<>();
        try (Connection conn = jdbcTemplate.getDataSource().getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();

            String schemaPattern = resolveSchema(schema, conn);

            try (ResultSet rs = metaData.getColumns(database, schemaPattern, table, "%")) {
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
     * Verifica si una tabla existe en la base de datos y esquema especificados.
     */
    public boolean tableExists(String database, String schema, String table) throws SQLException {
        try (Connection conn = jdbcTemplate.getDataSource().getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();

            String schemaPattern = resolveSchema(schema, conn);

            try (ResultSet rs = metaData.getTables(database, schemaPattern, table, null)) {
                return rs.next();
            }
        }
    }

    /**
     * Resuelve el esquema a utilizar: si se proporciona uno se usa, si no se
     * intenta detectar automáticamente.
     */
    public String resolveSchema(String schema, Connection conn) throws SQLException {
        if (schema != null && !schema.isEmpty()) {
            return schema;
        }

        try {
            String currentSchema = conn.getSchema();
            if (currentSchema != null && !currentSchema.isEmpty()) {
                return currentSchema;
            }
        } catch (Exception e) {
            log.warn("No se pudo obtener el esquema mediante conn.getSchema(), usando fallback por motor.");
        }

        DatabaseMetaData metaData = conn.getMetaData();
        String dbProduct = metaData.getDatabaseProductName().toLowerCase();

        // MEJORAR EN UN FUTURO, LO HICE RAPIDO
        if (dbProduct.contains("microsoft")) {
            return "dbo";
        } else if (dbProduct.contains("postgresql") || dbProduct.contains("redshift")) {
            return "public";
        } else if (dbProduct.contains("oracle") || dbProduct.contains("db2") || dbProduct.contains("hana")) {
            return metaData.getUserName();
        } else if (dbProduct.contains("mysql") || dbProduct.contains("mariadb") || dbProduct.contains("sqlite")) {
            return null;
        } else if (dbProduct.contains("h2")) {
            return "PUBLIC";
        } else if (dbProduct.contains("snowflake")) {
            return "PUBLIC";
        }

        return null;
    }
}
