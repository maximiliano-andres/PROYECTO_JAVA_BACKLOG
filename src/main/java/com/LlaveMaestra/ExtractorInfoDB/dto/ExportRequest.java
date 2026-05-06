package com.LlaveMaestra.ExtractorInfoDB.dto;

import lombok.Data;
import java.util.List;

@Data
public class ExportRequest {
    private String database;
    private String schema;
    private String table;
    private String jobId;

    private ExportEnums.ExportFormat format;
    private ExportEnums.ExportScope scope;

    // Configuración de alcance
    private Integer page; // Para scope=PAGE
    private Integer pageSize; // Para scope=PAGE
    private Integer pageFrom; // Para scope=PAGES
    private Integer pageTo; // Para scope=PAGES
    private Integer rowCount; // Para scope=ROWS
    private Integer rowFrom; // Para scope=RANGE
    private Integer rowTo; // Para scope=RANGE

    // Selección de columnas
    private List<String> columns;

    // Filtros dinámicos
    // Ejemplo: [{ "column": "id", "operator": ">", "value": 10 }]
    private List<FilterRule> filters;

    @Data
    public static class FilterRule {
        private String column;
        private String operator;
        private Object value;
    }
}
