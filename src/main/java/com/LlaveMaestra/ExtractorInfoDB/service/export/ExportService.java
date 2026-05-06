package com.LlaveMaestra.ExtractorInfoDB.service.export;

import com.LlaveMaestra.ExtractorInfoDB.dto.ExportEnums;
import com.LlaveMaestra.ExtractorInfoDB.dto.ExportRequest;
import com.LlaveMaestra.ExtractorInfoDB.service.security.SecurityValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExportService {

    private final JdbcTemplate jdbcTemplate;
    private final SecurityValidator securityValidator;
    private final Map<String, ExportStrategy> strategies;

    /**
     * Ejecuta el proceso de exportación completo.
     */
    public void executeExport(ExportRequest request, OutputStream outputStream) throws Exception {
        // 1. Obtener estrategia
        ExportStrategy strategy = getStrategy(request.getFormat());
        
        // 2. Validar tabla y generar nombre seguro
        securityValidator.validateTableExistence(request.getDatabase(), request.getSchema(), request.getTable());
        String safeTableName = securityValidator.getSafeTableName(request.getDatabase(), request.getSchema(), request.getTable());

        // 3. Construir Consulta SQL
        SqlQueryBuilder queryBuilder = buildSqlQuery(request, safeTableName);
        
        log.info("Iniciando exportación [{}] con SQL: {}", request.getFormat(), queryBuilder.sql);

        // 4. Ejecutar con Streaming
        // Usamos JDBC puro para asegurar que el ResultSet no cargue todo en memoria (FetchSize)
        try (Connection conn = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(queryBuilder.sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            
            // Configurar el fetch size para streaming (específico de cada driver, 1000 es balanceado)
            ps.setFetchSize(1000);
            
            // Setear parámetros si existen (filtros)
            for (int i = 0; i < queryBuilder.params.size(); i++) {
                ps.setObject(i + 1, queryBuilder.params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                List<String> columns = request.getColumns();
                if (columns == null || columns.isEmpty()) {
                    columns = new ArrayList<>();
                    int count = rs.getMetaData().getColumnCount();
                    for (int i = 1; i <= count; i++) {
                        columns.add(rs.getMetaData().getColumnLabel(i));
                    }
                }
                strategy.write(rs, columns, outputStream);
            }
        }
    }

    private ExportStrategy getStrategy(ExportEnums.ExportFormat format) {
        String beanName = format.name().toLowerCase() + "ExportStrategy";
        ExportStrategy strategy = strategies.get(beanName);
        if (strategy == null) {
            throw new IllegalArgumentException("Formato de exportación no soportado: " + format);
        }
        return strategy;
    }

    private SqlQueryBuilder buildSqlQuery(ExportRequest request, String safeTableName) {
        StringBuilder sql = new StringBuilder("SELECT ");
        
        // Columnas
        if (request.getColumns() == null || request.getColumns().isEmpty()) {
            sql.append("*");
        } else {
            sql.append(request.getColumns().stream()
                .map(c -> "[" + c + "]") // Simple quoting, SecurityValidator podría mejorarse para esto
                .collect(Collectors.joining(", ")));
        }

        sql.append(" FROM ").append(safeTableName);

        List<Object> params = new ArrayList<>();
        
        // Filtros (Simplificado para el ejemplo)
        if (request.getFilters() != null && !request.getFilters().isEmpty()) {
            sql.append(" WHERE ");
            List<String> filterSql = new ArrayList<>();
            for (ExportRequest.FilterRule filter : request.getFilters()) {
                // Validación básica de columna para evitar inyección en el nombre de columna
                if (filter.getColumn().matches("^[a-zA-Z0-9_]+$")) {
                    filterSql.add("[" + filter.getColumn() + "] " + filter.getOperator() + " ?");
                    params.add(filter.getValue());
                }
            }
            sql.append(String.join(" AND ", filterSql));
        }

        // Alcance (Scope) - Optimizado para SQL Server
        switch (request.getScope()) {
            case PAGE:
                int offset = request.getPage() * request.getPageSize();
                sql.append(" ORDER BY (SELECT NULL) OFFSET ").append(offset).append(" ROWS FETCH NEXT ").append(request.getPageSize()).append(" ROWS ONLY");
                break;
            case PAGES:
                int fromOffset = (request.getPageFrom() - 1) * 100; // Asumiendo 100 por página si no se especifica
                int totalRows = (request.getPageTo() - request.getPageFrom() + 1) * 100;
                sql.append(" ORDER BY (SELECT NULL) OFFSET ").append(fromOffset).append(" ROWS FETCH NEXT ").append(totalRows).append(" ROWS ONLY");
                break;
            case ROWS:
                // SQL Server TOP es más rápido si no hay orden
                sql.insert(7, "TOP (" + request.getRowCount() + ") "); 
                break;
            case RANGE:
                int start = request.getRowFrom() - 1;
                int count = request.getRowTo() - request.getRowFrom() + 1;
                sql.append(" ORDER BY (SELECT NULL) OFFSET ").append(start).append(" ROWS FETCH NEXT ").append(count).append(" ROWS ONLY");
                break;
            case ALL:
            default:
                // No limit
                break;
        }

        return new SqlQueryBuilder(sql.toString(), params);
    }

    private static class SqlQueryBuilder {
        String sql;
        List<Object> params;
        SqlQueryBuilder(String sql, List<Object> params) {
            this.sql = sql;
            this.params = params;
        }
    }
}
