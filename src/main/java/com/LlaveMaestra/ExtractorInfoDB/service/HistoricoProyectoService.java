package com.LlaveMaestra.ExtractorInfoDB.service;

// service/HistoricoProyectoService.java

import com.LlaveMaestra.ExtractorInfoDB.dto.response.HistoricoProyectoResponse;
import com.LlaveMaestra.ExtractorInfoDB.repository.HistoricoProyectoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio de negocio — orquesta repositorio y mapeo.
 * Principio: S (Single Responsibility) — solo lógica de negocio, sin HTTP ni
 * SQL.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HistoricoProyectoService {

    private final HistoricoProyectoRepository repository;

    public List<HistoricoProyectoResponse> obtenerTopDiez() {
        log.info("Obteniendo top 10 historicos de proyectos");
        var dominio = repository.findTopDiez();
        log.info("Se obtuvieron {} registros", dominio.size());
        return HistoricoProyectoResponse.fromList(dominio);
    }

    public List<HistoricoProyectoResponse> obtenerPorAnioYMes(int anio, int mes) {
        validarAnioMes(anio, mes);
        log.info("Obteniendo historicos por año={} mes={}", anio, mes);
        return HistoricoProyectoResponse.fromList(
                repository.findByAnioYMes(anio, mes));
    }

    private void validarAnioMes(int anio, int mes) {
        if (mes < 1 || mes > 12) {
            throw new IllegalArgumentException("Mes inválido: " + mes + ". Debe ser entre 1 y 12.");
        }
        if (anio < 2000 || anio > 2100) {
            throw new IllegalArgumentException("Año inválido: " + anio);
        }
    }
}