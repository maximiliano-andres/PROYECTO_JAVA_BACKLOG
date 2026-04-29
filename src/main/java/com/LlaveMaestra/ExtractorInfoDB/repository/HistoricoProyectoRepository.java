package com.LlaveMaestra.ExtractorInfoDB.repository;

import com.LlaveMaestra.ExtractorInfoDB.domain.HistoricoProyecto;
import java.util.List;

/**
 * Contrato de repositorio — solo la interfaz en la capa de dominio.
 * Principio: I (Interface Segregation) + D (Dependency Inversion).
 */
public interface HistoricoProyectoRepository {
    List<HistoricoProyecto> findTopMil();

    List<HistoricoProyecto> findByAnioYMes(int anio, int mes);
}