package com.LlaveMaestra.ExtractorInfoDB.service.export;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Component;

import com.LlaveMaestra.ExtractorInfoDB.service.storage.StorageService;
import lombok.RequiredArgsConstructor;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.util.List;

@Component
@RequiredArgsConstructor
public class XlsxExportStrategy implements ExportStrategy {

    private final StorageService storageService;
    private final ExportProgressService progressService;

    @Override
    public void write(ResultSet rs, List<String> columns, OutputStream os, String jobId) throws Exception {
        // Obtener el mejor directorio basado en espacio en disco
        java.nio.file.Path tempPath = storageService.getBestTempDirectory();
        java.io.File tempDir = tempPath.toFile();
        
        org.apache.poi.util.TempFile.setTempFileCreationStrategy(
            new org.apache.poi.util.DefaultTempFileCreationStrategy(tempDir));

        // SXSSFWorkbook guarda registros en disco (temp) para mantener bajo uso de RAM
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
            // Optimización: Comprimir archivos temporales para ahorrar espacio en disco
            workbook.setCompressTempFiles(true);
            
            SXSSFSheet sheet = workbook.createSheet("Datos");
            
            // Estilo Header
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Header Row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns.get(i));
                cell.setCellStyle(headerStyle);
            }

            // Data Rows
            int rowNum = 1;
            int colCount = columns.size();
            while (rs.next()) {
                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < colCount; i++) {
                    Object val = rs.getObject(columns.get(i));
                    if (val != null) {
                        if (val instanceof Number) {
                            row.createCell(i).setCellValue(((Number) val).doubleValue());
                        } else if (val instanceof Boolean) {
                            row.createCell(i).setCellValue((Boolean) val);
                        } else {
                            row.createCell(i).setCellValue(val.toString());
                        }
                    }
                }
                
                // Logging de progreso cada 10.000 filas
                if (rowNum % 10000 == 0) {
                    String msg = "Excel Progress: " + rowNum + " rows written...";
                    System.out.println(msg);
                    progressService.publishProgress(jobId, msg);
                }
            }
            
            String msgFin = "Escritura de datos Excel finalizada. Generando archivo...";
            System.out.println(msgFin);
            progressService.publishProgress(jobId, msgFin);
            
            workbook.write(os);
            workbook.dispose(); // Elimina archivos temporales de disco
            
            String msgOk = "Archivo Excel generado exitosamente.";
            System.out.println(msgOk);
            progressService.publishProgress(jobId, msgOk);
            progressService.completeProgress(jobId);
        }
    }

    @Override
    public String getContentType() {
        return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    }

    @Override
    public String getFileExtension() {
        return "xlsx";
    }
}
