package com.LlaveMaestra.ExtractorInfoDB.service.export;

import com.LlaveMaestra.ExtractorInfoDB.service.storage.StorageService;
import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.ResultSet;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CsvExportStrategy implements ExportStrategy {

    private final StorageService storageService;
    private final ExportProgressService progressService;

    @Override
    public void write(ResultSet rs, List<String> columns, OutputStream os, String jobId) throws Exception {
        // 1. Crear archivo temporal en el mejor disco
        java.nio.file.Path tempPath = storageService.getBestTempDirectory();
        File tempFile = File.createTempFile("export_", ".csv", tempPath.toFile());

        try {
            // 2. Escribir al archivo temporal
            try (CSVWriter writer = new CSVWriter(new FileWriter(tempFile, StandardCharsets.UTF_8))) {
                // Header
                String[] header = columns.toArray(new String[0]);
                writer.writeNext(header);

                // Data
                int rowCount = 0;
                int colCount = columns.size();
                while (rs.next()) {
                    String[] row = new String[colCount];
                    for (int i = 0; i < colCount; i++) {
                        Object val = rs.getObject(columns.get(i));
                        row[i] = val != null ? val.toString() : "";
                    }
                    writer.writeNext(row);
                    rowCount++;

                    if (rowCount % 10000 == 0) {
                        String msg = "CSV Progress: " + rowCount + " rows written to temp file...";
                        System.out.println(msg);
                        progressService.publishProgress(jobId, msg);
                    }
                }
                writer.flush();
                String msgFin = "Escritura CSV en disco finalizada. Enviando al cliente...";
                System.out.println(msgFin);
                progressService.publishProgress(jobId, msgFin);
            }

            // 3. Copiar el archivo al output stream de respuesta
            Files.copy(tempFile.toPath(), os);
            os.flush();
            String msgOk = "Envío CSV completado.";
            System.out.println(msgOk);
            progressService.publishProgress(jobId, msgOk);
            progressService.completeProgress(jobId);

        } finally {
            // 4. Limpiar siempre el archivo temporal
            if (tempFile.exists()) {
                tempFile.delete();
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
