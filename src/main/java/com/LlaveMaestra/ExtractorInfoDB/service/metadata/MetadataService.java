package com.LlaveMaestra.ExtractorInfoDB.service.metadata;

import com.LlaveMaestra.ExtractorInfoDB.config.AppProperties;
import com.LlaveMaestra.ExtractorInfoDB.dto.ColumnInfo;
import com.LlaveMaestra.ExtractorInfoDB.dto.DatabaseConnectionDTO;
import com.LlaveMaestra.ExtractorInfoDB.dto.DatabaseInfo;
import com.LlaveMaestra.ExtractorInfoDB.dto.TableInfo;
import com.LlaveMaestra.ExtractorInfoDB.service.DatabaseConnectionService;
import com.LlaveMaestra.ExtractorInfoDB.strategy.DatabaseEngineStrategy;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final DatabaseConnectionService connectionService;
    private final HttpSession httpSession;

    /**
     * Obtiene la lista de bases de datos (catálogos) disponibles en el servidor.
     */
    public List<DatabaseInfo> getDatabases() throws SQLException {
        DatabaseConnectionDTO config = connectionService.getConnectionConfig(httpSession.getId());
        if (config == null) return new ArrayList<>();

        // Si no es modo admin, solo devolvemos la base de datos conectada
        if (!config.isAdminMode()) {
            List<DatabaseInfo> singleDb = new ArrayList<>();
            singleDb.add(new DatabaseInfo(config.getDatabaseName()));
            return singleDb;
        }

        DatabaseEngineStrategy strategy = connectionService.getStrategyForSession(httpSession.getId());
        List<DatabaseInfo> databases = new ArrayList<>();
        
        try (Connection conn = jdbcTemplate.getDataSource().getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            
            if (strategy != null && strategy.supportsCatalogs()) {
                try (ResultSet rs = metaData.getCatalogs()) {
                    while (rs.next()) {
                        String dbName = rs.getString("TABLE_CAT");
                        if (!appProperties.getExcludedDatabases().contains(dbName)) {
                            databases.add(new DatabaseInfo(dbName));
                        }
                    }
                }
            } else {
                // Fallback si no soporta catálogos o es motor de esquemas
                databases.add(new DatabaseInfo(config.getDatabaseName()));
            }
        }
        return databases;
    }

    /**
     * Obtiene las tablas de una base de datos y esquema específicos.
     * Si el esquema es nulo, intenta detectarlo automáticamente.
     */
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
        DatabaseEngineStrategy strategy = connectionService.getStrategyForSession(httpSession.getId());
        if (strategy != null) {
            return strategy.resolveSchema(conn, schema);
        }
        
        // Fallback original si no hay estrategia
        if (schema != null && !schema.isEmpty()) {
            return schema;
        }
        return null;
    }
}
