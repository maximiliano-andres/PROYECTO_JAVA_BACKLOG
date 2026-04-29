package com.LlaveMaestra.ExtractorInfoDB.controller;

import com.LlaveMaestra.ExtractorInfoDB.adapter.HistoricoProyecto;
import com.LlaveMaestra.ExtractorInfoDB.database.DynamicQueryExecutor;
import com.LlaveMaestra.ExtractorInfoDB.service.HistoricoProyectoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/proyectos")
public class HistoricoProyectoController {

    private final HistoricoProyectoService service;
    private final DynamicQueryExecutor executor;

    public HistoricoProyectoController(HistoricoProyectoService service,
            DynamicQueryExecutor executor) {
        this.service = service;
        this.executor = executor;
    }

    @PostMapping("/query")
    public ResponseEntity<List<HistoricoProyecto>> ejecutarQuery(
            @RequestBody QueryRequest request) {

        List<HistoricoProyecto> data = service.obtener(request.getSql());
        return ResponseEntity.ok(data);
    }

    @GetMapping("/historico")
    public List<HistoricoProyecto> getHistorico() {
        return service.obtener("SELECT TOP (100) * FROM HistoricoProyecto_Honorarios");
        // return service.obtener("SELECT TOP (100) * FROM Bancos");
    }

    /**
     * DIAGNÓSTICO: muestra las columnas reales de la tabla y una fila de ejemplo.
     * Llamar a: GET /api/v1/proyectos/columnas
     */
    public ResponseEntity<List<Map<String, Object>>> verColumnas() {
        List<Map<String, Object>> fila = executor.execute(
                "SELECT TOP 1 * FROM HistoricoProyecto_Honorarios");
        return ResponseEntity.ok(fila);
    }

    /**
     * DIAGNÓSTICO: muestra todas las tablas disponibles en la base de datos activa.
     * Llamar a: GET /api/v1/proyectos/tablas
     */
    @GetMapping("/tablas")
    public ResponseEntity<List<Map<String, Object>>> verTablas() {
        List<Map<String, Object>> tablas = executor.execute(
                "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE' ORDER BY TABLE_NAME");
        return ResponseEntity.ok(tablas);
    }
}