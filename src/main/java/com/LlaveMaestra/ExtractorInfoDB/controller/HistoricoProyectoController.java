// controller/HistoricoProyectoController.java
package com.LlaveMaestra.ExtractorInfoDB.controller;

import com.LlaveMaestra.ExtractorInfoDB.dto.response.HistoricoProyectoResponse;
import com.LlaveMaestra.ExtractorInfoDB.service.HistoricoProyectoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST — solo maneja HTTP, delega todo al servicio.
 * Principio: S (Single Responsibility) — capa HTTP pura.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/historico-proyectos")
@RequiredArgsConstructor
public class HistoricoProyectoController {

    private final HistoricoProyectoService service;

    /**
     * GET /api/v1/historico-proyectos/top
     * Retorna los 10 primeros registros de HistoricoProyecto_Honorarios
     */
    @GetMapping("/top")
    public ResponseEntity<List<HistoricoProyectoResponse>> obtenerTop() {
        return ResponseEntity.ok(service.obtenerTopDiez());
    }

    /**
     * GET /api/v1/historico-proyectos?anio=2024&mes=3
     * Filtra por año y mes
     */
    @GetMapping
    public ResponseEntity<List<HistoricoProyectoResponse>> obtenerPorFiltro(
            @RequestParam int anio,
            @RequestParam int mes) {
        return ResponseEntity.ok(service.obtenerPorAnioYMes(anio, mes));
    }
}