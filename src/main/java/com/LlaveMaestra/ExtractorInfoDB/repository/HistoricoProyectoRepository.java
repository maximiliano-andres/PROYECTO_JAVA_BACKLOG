package com.LlaveMaestra.ExtractorInfoDB.repository;

import com.LlaveMaestra.ExtractorInfoDB.database.DatabaseConnection;
import com.LlaveMaestra.ExtractorInfoDB.model.record.HistoricoProyectoHonorarios;
import com.LlaveMaestra.ExtractorInfoDB.utils.AppLogger;

import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.time.LocalDate;

@Repository
public final class HistoricoProyectoRepository {

    private static final String FIND_ALL = """
            SELECT TOP (1000)
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

    private static final String FIND_BY_FUNCIONARIO = FIND_ALL.replace(
            "FROM", "WHERE [Id_Funcionario] = ? FROM" // SQL Server usa WHERE antes del FROM
    // Mejor así ↓
    );

    // Query separada y clara para filtro por funcionario
    private static final String FIND_BY_FUNCIONARIO_ID = """
            SELECT TOP (1000)
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
            WHERE [Id_Funcionario] = ?
            """;

    private static final String FIND_BY_ANIO_MES = """
            SELECT TOP (1000)
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
            WHERE [Ano] = ? AND [Mes] = ?
            """;

    // ─── Public API ───────────────────────────────────────────────────────────

    public List<HistoricoProyectoHonorarios> findAll() {
        return executeQuery(FIND_ALL, ps -> {
        });
    }

    public List<HistoricoProyectoHonorarios> findByFuncionario(int idFuncionario) {
        return executeQuery(FIND_BY_FUNCIONARIO_ID, ps -> ps.setInt(1, idFuncionario));
    }

    public List<HistoricoProyectoHonorarios> findByAnioMes(int anio, int mes) {
        return executeQuery(FIND_BY_ANIO_MES, ps -> {
            ps.setInt(1, anio);
            ps.setInt(2, mes);
        });
    }

    // ─── Mapper ───────────────────────────────────────────────────────────────

    private HistoricoProyectoHonorarios mapRow(ResultSet rs) throws SQLException {
        return new HistoricoProyectoHonorarios(
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
    }

    // ─── Infraestructura interna ──────────────────────────────────────────────

    // Interface funcional para binding de parámetros sin boilerplate
    @FunctionalInterface
    private interface StatementBinder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    private final DatabaseConnection databaseConnection;

    public HistoricoProyectoRepository(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    private List<HistoricoProyectoHonorarios> executeQuery(String sql, StatementBinder binder) {
        List<HistoricoProyectoHonorarios> resultado = new ArrayList<>();

        try (Connection conn = databaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            binder.bind(ps);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            AppLogger.logError("Error en query: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return resultado;
    }

    // Conversión segura — sql.Date puede ser null
    private LocalDate toLocalDate(java.sql.Date date) {
        return date != null ? date.toLocalDate() : null;
    }
}