// dto/HistoricoProyectoResponse.java
package com.LlaveMaestra.ExtractorInfoDB.dto.response;

import com.LlaveMaestra.ExtractorInfoDB.domain.HistoricoProyecto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO de respuesta — el cliente recibe esto, nunca el dominio directamente.
 * Principio: S (Single Responsibility) — solo transporte de datos hacia API.
 */
public record HistoricoProyectoResponse(
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
    /** Factory method — conversión limpia de dominio a DTO */
    public static HistoricoProyectoResponse from(HistoricoProyecto dominio) {
        return new HistoricoProyectoResponse(
                dominio.idArea(), dominio.idFuncionario(), dominio.idProyecto(),
                dominio.mes(), dominio.ano(),
                dominio.fecInicioContrato(), dominio.fecFinalContrato(),
                dominio.cargo(), dominio.nroDecreto(), dominio.fechaDecreto(),
                dominio.jornada(), dominio.idGrado(), dominio.idDireccion(),
                dominio.idDepartamento(), dominio.idSeccion(), dominio.glosa(),
                dominio.numObligacion(), dominio.montoPactado(), dominio.fechaContrato(),
                dominio.historicoCorrelativoProyectos(),
                dominio.idFuncion(), dominio.idHora(),
                dominio.idEstablecimiento(), dominio.idOficina());
    }

    /** Conversión de lista completa en stream eficiente */
    public static List<HistoricoProyectoResponse> fromList(List<HistoricoProyecto> lista) {
        return lista.stream().map(HistoricoProyectoResponse::from).toList();
    }
}