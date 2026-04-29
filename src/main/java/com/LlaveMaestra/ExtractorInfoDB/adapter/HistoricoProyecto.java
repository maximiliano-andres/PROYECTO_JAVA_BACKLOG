package com.LlaveMaestra.ExtractorInfoDB.adapter;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Modelo que representa una fila de la tabla HistoricoProyecto_Honorarios.
 * Los nombres de los campos coinciden exactamente con las columnas de la tabla.
 */
@Getter
@Setter
public class HistoricoProyecto {

    private Integer id_seccion;
    private Integer ano;
    private Integer id_hora;
    private Integer id_funcionario;
    private Integer jornada;
    private Integer id_oficina;
    private Integer id_proyecto;
    private String glosa;
    private Integer id_direccion;
    private OffsetDateTime fecha_contrato;
    private Integer id_funcion;
    private Integer id_area;
    private OffsetDateTime fecha_decreto;
    private OffsetDateTime fec_iniciocontrato;
    private String historicocorrelativoproyectos;
    private BigDecimal monto_pactado;
    private Integer id_departamento;
    private String id_establecimiento;
    private OffsetDateTime fec_finalcontrato;
    private Integer num_obligacion;
    private Integer mes;
    private String nro_decreto;
    private Integer id_grado;
    private String cargo;
}
