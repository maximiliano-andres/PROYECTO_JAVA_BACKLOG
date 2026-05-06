package com.LlaveMaestra.ExtractorInfoDB.controller;

import com.LlaveMaestra.ExtractorInfoDB.dto.ExportEnums;
import com.LlaveMaestra.ExtractorInfoDB.dto.ExportRequest;
import com.LlaveMaestra.ExtractorInfoDB.service.export.ExportService;
import com.LlaveMaestra.ExtractorInfoDB.service.export.ExportStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.Map;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
@Slf4j
public class ExportController {

    private final ExportService exportService;
    private final Map<String, ExportStrategy> strategies;

    @PostMapping
    public ResponseEntity<StreamingResponseBody> exportData(@RequestBody ExportRequest request) {
        log.info("Recibida solicitud de exportación: {} en formato {}", request.getTable(), request.getFormat());

        try {
            // Validar que la estrategia existe antes de empezar
            String beanName = request.getFormat().name().toLowerCase() + "ExportStrategy";
            ExportStrategy strategy = strategies.get(beanName);

            if (strategy == null) {
                log.error("Estrategia no encontrada para formato: {}", request.getFormat());
                return ResponseEntity.badRequest().build();
            }

            String fileName = String.format("export_%s_%s.%s",
                    request.getTable(),
                    System.currentTimeMillis(),
                    strategy.getFileExtension());

            StreamingResponseBody responseBody = outputStream -> {
                try {
                    exportService.executeExport(request, outputStream);
                } catch (Exception e) {
                    log.error("Error durante el streaming de exportación", e);
                }
            };

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.parseMediaType(strategy.getContentType()))
                    .body(responseBody);

        } catch (Exception e) {
            log.error("Error al preparar la exportación", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
