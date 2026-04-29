package com.LlaveMaestra.ExtractorInfoDB.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad de dominio pura — sin dependencias de frameworks.
 * Principio: D (Dependency Inversion) — el dominio no depende de nada externo.
 */
public record HistoricoProyecto(
        Integer idArea,
        Integer idFuncionario,
        Integer idProyecto,
        Integer mes,
        Integer ano,
        LocalDate fecInicioContrato,
        LocalDate fecFinalContrato,
        String cargo,
        String nroDecreto,
        LocalDate fechaDecreto,
        String jornada,
        Integer idGrado,
        Integer idDireccion,
        Integer idDepartamento,
        Integer idSeccion,
        String glosa,
        String numObligacion,
        BigDecimal montoPactado,
        LocalDate fechaContrato,
        Integer historicoCorrelativoProyectos,
        Integer idFuncion,
        Integer idHora,
        Integer idEstablecimiento,
        Integer idOficina) {
}