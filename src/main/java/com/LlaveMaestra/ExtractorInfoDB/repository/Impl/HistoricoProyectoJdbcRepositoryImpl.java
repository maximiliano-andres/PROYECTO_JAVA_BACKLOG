package com.LlaveMaestra.ExtractorInfoDB.repository.Impl;

// repository/impl/HistoricoProyectoJdbcRepository.java

import com.LlaveMaestra.ExtractorInfoDB.domain.HistoricoProyecto;
import com.LlaveMaestra.ExtractorInfoDB.repository.HistoricoProyectoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Implementación JDBC nativa — máximo rendimiento sin ORM.
 * Principio: O (Open/Closed) — nuevo datasource = nueva impl, sin tocar
 * dominio.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class HistoricoProyectoJdbcRepositoryImpl implements HistoricoProyectoRepository {

        private final JdbcTemplate jdbcTemplate;

        // SQL como constante — compilado una sola vez, reutilizable
        private static final String SQL_TOP_DIEZ = """
                        SELECT TOP (100000)
                        [Id_Area], [Id_Funcionario], [Id_Proyecto],
                        [Mes], [Ano],
                        [Fec_InicioContrato], [Fec_FinalContrato],
                        [Cargo], [Nro_Decreto], [Fecha_Decreto],
                        [Jornada], [Id_Grado], [Id_Direccion],
                        [Id_Departamento], [Id_Seccion], [Glosa],
                        [Num_Obligacion], [Monto_Pactado], [Fecha_Contrato],
                        [HistoricoCorrelativoProyectos],
                        [Id_Funcion], [Id_Hora], [Id_Establecimiento], [Id_Oficina]
                        FROM [Rem_Honorarios].[dbo].[HistoricoProyecto_Honorarios]
                        """;

        private static final String SQL_BY_ANIO_MES = SQL_TOP_DIEZ
                        .replace("SELECT TOP (10)", "SELECT TOP (10)")
                        + " WHERE [Ano] = ? AND [Mes] = ?";

        // RowMapper como campo estático — se instancia UNA sola vez (bajo consumo de
        // memoria)
        private static final RowMapper<HistoricoProyecto> ROW_MAPPER = new HistoricoProyectoRowMapper();

        @Override
        public List<HistoricoProyecto> findTopDiez() {
                log.debug("Ejecutando consulta: findTopDiez");
                return jdbcTemplate.query(SQL_TOP_DIEZ, ROW_MAPPER);
        }

        @Override
        public List<HistoricoProyecto> findByAnioYMes(int anio, int mes) {
                log.debug("Ejecutando consulta: findByAnioYMes({}, {})", anio, mes);
                return jdbcTemplate.query(SQL_BY_ANIO_MES, ROW_MAPPER, anio, mes);
        }

        /**
         * RowMapper estático e interno — encapsula el mapeo, reutilizable.
         * No usa lambdas para que la JVM pueda optimizarlo mejor (clase dedicada).
         */
        private static final class HistoricoProyectoRowMapper implements RowMapper<HistoricoProyecto> {

                @Override
                public HistoricoProyecto mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return new HistoricoProyecto(
                                        rs.getObject("Id_Area", Integer.class),
                                        rs.getObject("Id_Funcionario", Integer.class),
                                        rs.getObject("Id_Proyecto", Integer.class),
                                        rs.getObject("Mes", Integer.class),
                                        rs.getObject("Ano", Integer.class),
                                        rs.getDate("Fec_InicioContrato") != null
                                                        ? rs.getDate("Fec_InicioContrato").toLocalDate()
                                                        : null,
                                        rs.getDate("Fec_FinalContrato") != null
                                                        ? rs.getDate("Fec_FinalContrato").toLocalDate()
                                                        : null,
                                        rs.getString("Cargo"),
                                        rs.getString("Nro_Decreto"),
                                        rs.getDate("Fecha_Decreto") != null
                                                        ? rs.getDate("Fecha_Decreto").toLocalDate()
                                                        : null,
                                        rs.getString("Jornada"),
                                        rs.getObject("Id_Grado", Integer.class),
                                        rs.getObject("Id_Direccion", Integer.class),
                                        rs.getObject("Id_Departamento", Integer.class),
                                        rs.getObject("Id_Seccion", Integer.class),
                                        rs.getString("Glosa"),
                                        rs.getString("Num_Obligacion"),
                                        rs.getBigDecimal("Monto_Pactado"),
                                        rs.getDate("Fecha_Contrato") != null
                                                        ? rs.getDate("Fecha_Contrato").toLocalDate()
                                                        : null,
                                        rs.getObject("HistoricoCorrelativoProyectos", Integer.class),
                                        rs.getObject("Id_Funcion", Integer.class),
                                        rs.getObject("Id_Hora", Integer.class),
                                        rs.getObject("Id_Establecimiento", Integer.class),
                                        rs.getObject("Id_Oficina", Integer.class));
                }
        }
}