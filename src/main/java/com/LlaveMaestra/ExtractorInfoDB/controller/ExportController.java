package com.LlaveMaestra.ExtractorInfoDB.controller;

import com.LlaveMaestra.ExtractorInfoDB.dto.ExportEnums;
import com.LlaveMaestra.ExtractorInfoDB.dto.ExportRequest;
import com.LlaveMaestra.ExtractorInfoDB.service.export.ExportService;
import com.LlaveMaestra.ExtractorInfoDB.service.export.ExportProgressService;
import com.LlaveMaestra.ExtractorInfoDB.service.export.ExportStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.Map;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
@Slf4j
public class ExportController {

    private final ExportService exportService;
    private final ExportProgressService progressService;
    private final Map<String, ExportStrategy> strategies;

    @GetMapping("/progress/{jobId}")
    public SseEmitter streamProgress(@PathVariable String jobId) {
        return progressService.createEmitter(jobId);
    }

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
                    log.info("Exportación completada exitosamente para tabla: {}", request.getTable());
                } catch (Exception e) {
                    log.error("CRITICAL: Fallo durante el streaming de exportación para la tabla: {}", request.getTable(), e);
                    // No podemos cambiar el status code aquí porque ya se enviaron los headers,
                    // pero al menos registramos el error completo.
                } finally {
                    outputStream.flush();
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
