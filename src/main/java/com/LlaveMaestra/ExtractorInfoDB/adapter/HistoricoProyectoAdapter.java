package com.LlaveMaestra.ExtractorInfoDB.adapter;

import com.LlaveMaestra.ExtractorInfoDB.util.MapUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class HistoricoProyectoAdapter implements SchemaAdapter<HistoricoProyecto> {

    @Override
    public HistoricoProyecto adapt(Map<String, Object> row) {

        HistoricoProyecto obj = new HistoricoProyecto();

        obj.setId_seccion(MapUtils.getInt(row, "id_seccion"));
        obj.setAno(MapUtils.getInt(row, "ano"));
        obj.setId_hora(MapUtils.getInt(row, "id_hora"));
        obj.setId_funcionario(MapUtils.getInt(row, "id_funcionario"));
        obj.setJornada(MapUtils.getInt(row, "jornada"));
        obj.setId_oficina(MapUtils.getInt(row, "id_oficina"));
        obj.setId_proyecto(MapUtils.getInt(row, "id_proyecto"));
        obj.setGlosa(MapUtils.getString(row, "glosa"));
        obj.setId_direccion(MapUtils.getInt(row, "id_direccion"));
        obj.setFecha_contrato(MapUtils.getOffsetDateTime(row, "fecha_contrato"));
        obj.setId_funcion(MapUtils.getInt(row, "id_funcion"));
        obj.setId_area(MapUtils.getInt(row, "id_area"));
        obj.setFecha_decreto(MapUtils.getOffsetDateTime(row, "fecha_decreto"));
        obj.setFec_iniciocontrato(MapUtils.getOffsetDateTime(row, "fec_iniciocontrato"));
        obj.setHistoricocorrelativoproyectos(MapUtils.getString(row, "historicocorrelativoproyectos"));
        obj.setMonto_pactado(MapUtils.getBigDecimal(row, "monto_pactado"));
        obj.setId_departamento(MapUtils.getInt(row, "id_departamento"));
        obj.setId_establecimiento(MapUtils.getString(row, "id_establecimiento"));
        obj.setFec_finalcontrato(MapUtils.getOffsetDateTime(row, "fec_finalcontrato"));
        obj.setNum_obligacion(MapUtils.getInt(row, "num_obligacion"));
        obj.setMes(MapUtils.getInt(row, "mes"));
        obj.setNro_decreto(MapUtils.getString(row, "nro_decreto"));
        obj.setId_grado(MapUtils.getInt(row, "id_grado"));
        obj.setCargo(MapUtils.getString(row, "cargo"));

        return obj;
    }
}