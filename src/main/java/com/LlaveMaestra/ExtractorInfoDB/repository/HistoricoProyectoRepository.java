package com.LlaveMaestra.ExtractorInfoDB.repository;

import com.LlaveMaestra.ExtractorInfoDB.model.record.HistoricoProyectoHonorarios;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class HistoricoProyectoRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    // Inyección de dependencias por constructor. Spring provee
    // NamedParameterJdbcTemplate automáticamente.
    public HistoricoProyectoRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String SELECT_BASE = """
            SELECT TOP (10)
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

    private static final String FIND_BY_FUNCIONARIO_ID = SELECT_BASE + " WHERE [Id_Funcionario] = :idFuncionario";

    private static final String FIND_BY_ANIO_MES = SELECT_BASE + " WHERE [Ano] = :anio AND [Mes] = :mes";

    // ─── RowMapper Reutilizable ───────────────────────────────────────────────

    private final RowMapper<HistoricoProyectoHonorarios> rowMapper = (rs, rowNum) -> new HistoricoProyectoHonorarios(
            rs.getInt("Id_Area"),
            rs.getInt("Id_Funcionario"),
            rs.getInt("Id_Proyecto"),
            rs.getInt("Mes"),
            rs.getInt("Ano"),
            toLocalDate(rs.getDate("Fec_InicioContrato")),
            toLocalDate(rs.getDate("Fec_FinalContrato")),
            rs.getString("Cargo"),
            rs.getString("Nro_Decreto"),
            toLocalDate(rs.getDate("Fecha_Decreto")),
            rs.getString("Jornada"),
            rs.getInt("Id_Grado"),
            rs.getInt("Id_Direccion"),
            rs.getInt("Id_Departamento"),
            rs.getInt("Id_Seccion"),
            rs.getString("Glosa"),
            rs.getString("Num_Obligacion"),
            rs.getBigDecimal("Monto_Pactado"),
            toLocalDate(rs.getDate("Fecha_Contrato")),
            rs.getInt("HistoricoCorrelativoProyectos"),
            rs.getInt("Id_Funcion"),
            rs.getInt("Id_Hora"),
            rs.getInt("Id_Establecimiento"),
            rs.getInt("Id_Oficina"));

    // ─── Public API ───────────────────────────────────────────────────────────

    public List<HistoricoProyectoHonorarios> findAll() {
        // query() con el RowMapper se encarga de abrir, iterar y cerrar el ResultSet,
        // PreparedStatement y Connection.
        return jdbcTemplate.query(SELECT_BASE, rowMapper);
    }

    public List<HistoricoProyectoHonorarios> findByFuncionario(int idFuncionario) {
        // Uso de parámetros nombrados para mayor seguridad y claridad
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idFuncionario", idFuncionario);

        return jdbcTemplate.query(FIND_BY_FUNCIONARIO_ID, params, rowMapper);
    }

    public List<HistoricoProyectoHonorarios> findByAnioMes(int anio, int mes) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("anio", anio)
                .addValue("mes", mes);

        return jdbcTemplate.query(FIND_BY_ANIO_MES, params, rowMapper);
    }

    // Conversión segura — sql.Date puede ser null
    private LocalDate toLocalDate(java.sql.Date date) {
        return date != null ? date.toLocalDate() : null;
    }
}