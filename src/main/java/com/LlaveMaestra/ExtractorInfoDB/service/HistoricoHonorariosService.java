package com.LlaveMaestra.ExtractorInfoDB.service;

import com.LlaveMaestra.ExtractorInfoDB.model.record.HistoricoProyectoHonorarios;
import com.LlaveMaestra.ExtractorInfoDB.repository.HistoricoProyectoRepository;
import com.LlaveMaestra.ExtractorInfoDB.utils.AppLogger;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Clase de Servicio que contiene la lógica de negocio para los históricos de honorarios.
 * Actúa como intermediario entre el Controlador y el Repositorio.
 */
@Service
public class HistoricoHonorariosService {

    private final HistoricoProyectoRepository repository;

    public HistoricoHonorariosService(HistoricoProyectoRepository repository) {
        this.repository = repository;
    }

    /**
     * Obtiene todos los honorarios desde el repositorio.
     * Aquí se podrían añadir filtros, transformaciones o reglas de negocio adicionales.
     */
    public List<HistoricoProyectoHonorarios> obtenerTodosLosHonorarios() {
        AppLogger.logInfo("Service: Recuperando datos desde el Repositorio");
        return repository.findAll();
    }
}
