package com.LlaveMaestra.ExtractorInfoDB.model.record;

import java.math.BigDecimal;
import java.time.LocalDate;

public record HistoricoProyectoHonorarios(
                int idArea,
                int idFuncionario,
                int idProyecto,
                int mes,
                int ano,
                LocalDate fechaInicioContrato,
                LocalDate fechaFinalContrato,
                String cargo,
                String nroDecreto,
                LocalDate fechaDecreto,
                String jornada,
                int idGrado,
                int idDireccion,
                int idDepartamento,
                int idSeccion,
                String glosa,
                String numObligacion,
                BigDecimal montoPactado,
                LocalDate fechaContrato,
                int historicoCorrelativoProyectos,
                int idFuncion,
                int idHora,
                int idEstablecimiento,
                int idOficina) {
}