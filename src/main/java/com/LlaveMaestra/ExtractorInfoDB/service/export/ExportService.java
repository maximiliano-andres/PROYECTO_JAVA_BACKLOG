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
        long startTime = System.currentTimeMillis();
        int rowCount = 0;

        try (Connection conn = jdbcTemplate.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(queryBuilder.sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            
            ps.setFetchSize(1000);
            
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
                
                log.info("Query ejecutada en {}ms. Iniciando escritura de datos...", (System.currentTimeMillis() - startTime));
                strategy.write(rs, columns, outputStream, request.getJobId());
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
                .map(c -> "[" + c + "]")
                .collect(Collectors.joining(", ")));
        }

        sql.append(" FROM ").append(safeTableName);

        List<Object> params = new ArrayList<>();
        
        // Filtros
        if (request.getFilters() != null && !request.getFilters().isEmpty()) {
            sql.append(" WHERE ");
            List<String> filterSql = new ArrayList<>();
            for (ExportRequest.FilterRule filter : request.getFilters()) {
                if (filter.getColumn() != null && filter.getColumn().matches("^[a-zA-Z0-9_]+$")) {
                    filterSql.add("[" + filter.getColumn() + "] " + filter.getOperator() + " ?");
                    params.add(filter.getValue());
                }
            }
            sql.append(String.join(" AND ", filterSql));
        }

        // Alcance (Scope) con validación de nulos
        if (request.getScope() == null) {
            return new SqlQueryBuilder(sql.toString(), params);
        }

        switch (request.getScope()) {
            case PAGE:
                int p = request.getPage() != null ? request.getPage() : 0;
                int ps = request.getPageSize() != null ? request.getPageSize() : 100;
                int offset = p * ps;
                sql.append(" ORDER BY (SELECT NULL) OFFSET ").append(offset).append(" ROWS FETCH NEXT ").append(ps).append(" ROWS ONLY");
                break;
            case PAGES:
                int from = request.getPageFrom() != null ? request.getPageFrom() : 1;
                int to = request.getPageTo() != null ? request.getPageTo() : from;
                int size = 100; // Default
                int off = (from - 1) * size;
                int total = (to - from + 1) * size;
                sql.append(" ORDER BY (SELECT NULL) OFFSET ").append(off).append(" ROWS FETCH NEXT ").append(total).append(" ROWS ONLY");
                break;
            case ROWS:
                int count = request.getRowCount() != null ? request.getRowCount() : 100;
                sql.insert(7, "TOP (" + count + ") "); 
                break;
            case RANGE:
                int rFrom = request.getRowFrom() != null ? request.getRowFrom() : 1;
                int rTo = request.getRowTo() != null ? request.getRowTo() : rFrom;
                int rStart = Math.max(0, rFrom - 1);
                int rCount = Math.max(0, rTo - rFrom + 1);
                sql.append(" ORDER BY (SELECT NULL) OFFSET ").append(rStart).append(" ROWS FETCH NEXT ").append(rCount).append(" ROWS ONLY");
                break;
            case ALL:
            default:
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
