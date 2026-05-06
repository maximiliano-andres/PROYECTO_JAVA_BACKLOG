package com.LlaveMaestra.ExtractorInfoDB.service.export;

import com.opencsv.CSVWriter;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.util.List;

@Component
public class CsvExportStrategy implements ExportStrategy {

    @Override
    public void write(ResultSet rs, List<String> columns, OutputStream os) throws Exception {
        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8))) {
            
            // Header
            String[] header = columns.toArray(new String[0]);
            writer.writeNext(header);

            // Data
            int colCount = columns.size();
            while (rs.next()) {
                String[] row = new String[colCount];
                for (int i = 0; i < colCount; i++) {
                    Object val = rs.getObject(columns.get(i));
                    row[i] = val != null ? val.toString() : "";
                }
                writer.writeNext(row);
                
                // Flush periódicamente para liberar memoria si el driver JDBC no lo hace
                if (rs.getRow() % 1000 == 0) {
                    writer.flush();
                }
            }
        }
    }

    @Override
    public String getContentType() {
        return "text/csv; charset=UTF-8";
    }

    @Override
    public String getFileExtension() {
        return "csv";
    }
}
