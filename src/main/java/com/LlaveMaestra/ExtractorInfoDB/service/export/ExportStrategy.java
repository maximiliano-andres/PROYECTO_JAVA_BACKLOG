package com.LlaveMaestra.ExtractorInfoDB.service.export;

import java.io.OutputStream;
import java.sql.ResultSet;
import java.util.List;

public interface ExportStrategy {
    /**
     * Escribe el contenido del ResultSet en el OutputStream de forma eficiente (streaming).
     */
    void write(ResultSet rs, List<String> columns, OutputStream os) throws Exception;
    
    /**
     * Devuelve el tipo de contenido HTTP apropiado.
     */
    String getContentType();
    
    /**
     * Devuelve la extensión de archivo.
     */
    String getFileExtension();
}
