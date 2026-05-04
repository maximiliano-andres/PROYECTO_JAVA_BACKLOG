package com.LlaveMaestra.ExtractorInfoDB.service.security;

import com.LlaveMaestra.ExtractorInfoDB.service.metadata.MetadataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

@Service
@Slf4j
@RequiredArgsConstructor
public class SecurityValidator {

    private final MetadataService metadataService;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Valida la existencia de una tabla para prevenir accesos no autorizados o errores de consulta.
     */
    public void validateTableExistence(String database, String schema, String table) throws SQLException {
        if (!metadataService.tableExists(database, schema, table)) {
            log.error("Intento de acceso a tabla inexistente: {}.{}.{}", database, schema, table);
            throw new IllegalArgumentException("La tabla especificada no existe.");
        }
    }

    /**
     * Genera un nombre de tabla seguro (escapado) adaptado al motor de base de datos.
     */
    public String getSafeTableName(String database, String schema, String table) {
        try (Connection conn = jdbcTemplate.getDataSource().getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            String quote = metaData.getIdentifierQuoteString();
            
            // Si quote es un espacio, el driver no soporta quoting
            if (" ".equals(quote)) quote = "";

            String effectiveSchema = metadataService.resolveSchema(schema, conn);
            
            StringBuilder sb = new StringBuilder();
            
            // Catálogo (Database)
            if (database != null && !database.isEmpty()) {
                sb.append(quote).append(database).append(quote).append(".");
            }
            
            // Esquema
            if (effectiveSchema != null && !effectiveSchema.isEmpty()) {
                sb.append(quote).append(effectiveSchema).append(quote).append(".");
            } else if (database != null && !database.isEmpty() && isSqlServer(metaData)) {
                // SQL Server permite [db]..[table] para usar el esquema por defecto
                sb.append("."); 
            }
            
            // Tabla
            sb.append(quote).append(table).append(quote);
            
            return sb.toString();
        } catch (SQLException e) {
            log.warn("Error al generar nombre seguro con metadata, usando fallback SQL Server: {}", e.getMessage());
            return String.format("[%s].[%s].[%s]", database, schema != null ? schema : "", table);
        }
    }

    private boolean isSqlServer(DatabaseMetaData metaData) throws SQLException {
        return metaData.getDatabaseProductName().toLowerCase().contains("microsoft");
    }
}
