package com.LlaveMaestra.ExtractorInfoDB.service.export;

import com.LlaveMaestra.ExtractorInfoDB.service.storage.StorageService;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.sql.ResultSet;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JsonExportStrategy implements ExportStrategy {

    private final StorageService storageService;
    private final ExportProgressService progressService;
    private final JsonFactory jsonFactory = new JsonFactory();

    @Override
    public void write(ResultSet rs, List<String> columns, OutputStream os, String jobId) throws Exception {
        log.info("Iniciando streaming directo de JSON para jobId: {}", jobId);
        
        // Configuramos el generador para que NO cierre el OutputStream original (os),
        // ya que Spring se encarga de eso.
        try (JsonGenerator jg = jsonFactory.createGenerator(os, com.fasterxml.jackson.core.JsonEncoding.UTF8)) {
            jg.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
            
            jg.writeStartArray();
            
            int rowCount = 0;
            int colCount = columns.size();
            while (rs.next()) {
                jg.writeStartObject();
                for (int i = 0; i < colCount; i++) {
                    String colName = columns.get(i);
                    Object val = rs.getObject(colName);
                    
                    if (val == null) {
                        jg.writeNullField(colName);
                    } else if (val instanceof Number) {
                        jg.writeNumberField(colName, ((Number) val).doubleValue());
                    } else if (val instanceof Boolean) {
                        jg.writeBooleanField(colName, (Boolean) val);
                    } else {
                        jg.writeStringField(colName, val.toString());
                    }
                }
                jg.writeEndObject();
                rowCount++;
                
                if (rowCount % 10000 == 0) {
                    String msg = "JSON Progress: " + rowCount + " rows streamed...";
                    log.info(msg);
                    progressService.publishProgress(jobId, msg);
                    // Forzamos el envío de datos parciales al cliente para mantener la conexión activa
                    jg.flush(); 
                }
            }
            
            jg.writeEndArray();
            jg.flush();
            
            String msgOk = "Exportación JSON finalizada (Total: " + rowCount + " filas).";
            log.info(msgOk);
            progressService.publishProgress(jobId, msgOk);
            progressService.completeProgress(jobId);
        }
    }

    @Override
    public String getContentType() {
        return "application/octet-stream";
    }

    @Override
    public String getFileExtension() {
        return "json";
    }
}
