package com.LlaveMaestra.ExtractorInfoDB.controller;

import com.LlaveMaestra.ExtractorInfoDB.adapter.HistoricoProyecto;
import com.LlaveMaestra.ExtractorInfoDB.database.DynamicQueryExecutor;
import com.LlaveMaestra.ExtractorInfoDB.service.HistoricoProyectoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.LlaveMaestra.ExtractorInfoDB.util.Wrapper;

import java.util.List;
import java.util.Map;

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
    public ResponseEntity<Wrapper<List<HistoricoProyecto>>> getHistorico() {
            List<HistoricoProyecto> data = service.obtener("SELECT TOP (100) * FROM HistoricoProyecto_Honorarios");
            return ResponseEntity.ok(Wrapper.success("Historico Proyecto retrieved successfully", data));       
        
    }
}