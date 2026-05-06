package com.LlaveMaestra.ExtractorInfoDB.service.export;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.sql.ResultSet;
import java.util.List;

@Component
public class XlsxExportStrategy implements ExportStrategy {

    @Override
    public void write(ResultSet rs, List<String> columns, OutputStream os) throws Exception {
        // SXSSFWorkbook guarda registros en disco (temp) para mantener bajo uso de RAM
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
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
            }
            
            workbook.write(os);
            workbook.dispose(); // Elimina archivos temporales de disco
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
