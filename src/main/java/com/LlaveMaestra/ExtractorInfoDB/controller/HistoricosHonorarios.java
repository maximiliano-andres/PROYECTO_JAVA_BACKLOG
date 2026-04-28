package com.LlaveMaestra.ExtractorInfoDB.controller;

import com.LlaveMaestra.ExtractorInfoDB.model.record.HistoricoProyectoHonorarios;
import com.LlaveMaestra.ExtractorInfoDB.service.HistoricoHonorariosService;
import com.LlaveMaestra.ExtractorInfoDB.utils.AppLogger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST que expone los endpoints relacionados con los Históricos de Honorarios.
 * Sigue el patrón MVC, actuando como la capa de entrada (Controller).
 */
@RestController
@RequestMapping("/historicos")
public class HistoricosHonorarios {

    // Inyección de dependencias por constructor (Práctica profesional recomendada)
    private final HistoricoHonorariosService service;

    public HistoricosHonorarios(HistoricoHonorariosService service) {
        this.service = service;
    }

    /**
     * Endpoint para obtener la lista completa de honorarios.
     * Ruta: GET http://localhost:8000/historicos/honorarios
     */
    @GetMapping("/honorarios")
    public List<HistoricoProyectoHonorarios> getHonorarios() {
        AppLogger.logInfo("HTTP GET: Solicitando históricos de honorarios");
        return service.obtenerTodosLosHonorarios();
    }
}
