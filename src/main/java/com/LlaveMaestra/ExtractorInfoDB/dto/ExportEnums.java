package com.LlaveMaestra.ExtractorInfoDB.dto;

public class ExportEnums {
    
    public enum ExportFormat {
        CSV, XLSX, JSON
    }

    public enum ExportScope {
        ALL,    // Todas las filas
        PAGE,   // Página actual
        PAGES,  // Rango de páginas
        ROWS,   // Primeras N filas
        RANGE   // Rango de filas (desde/hasta)
    }
}
