package com.LlaveMaestra.ExtractorInfoDB.service.export;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.sql.ResultSet;
import java.util.List;

@Component
public class JsonExportStrategy implements ExportStrategy {

    private final JsonFactory jsonFactory = new JsonFactory();

    @Override
    public void write(ResultSet rs, List<String> columns, OutputStream os) throws Exception {
        try (JsonGenerator jg = jsonFactory.createGenerator(os)) {
            jg.writeStartArray();
            
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
            }
            
            jg.writeEndArray();
            jg.flush();
        }
    }

    @Override
    public String getContentType() {
        return "application/json";
    }

    @Override
    public String getFileExtension() {
        return "json";
    }
}
