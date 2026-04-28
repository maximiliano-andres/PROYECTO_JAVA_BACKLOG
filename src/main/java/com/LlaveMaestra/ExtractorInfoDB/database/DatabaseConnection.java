package com.LlaveMaestra.ExtractorInfoDB.database;

import java.sql.Connection;
import java.sql.SQLException;
import jakarta.annotation.PreDestroy;
import javax.sql.DataSource;

import com.LlaveMaestra.ExtractorInfoDB.config.DatabaseProperties;
import com.LlaveMaestra.ExtractorInfoDB.utils.AppLogger;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.stereotype.Component;

/**
 * Gestiona la conexión a la base de datos usando HikariCP.
 * Ahora es un componente de Spring, lo que permite inyectar las configuraciones
 * automáticamente.
 */
@Component
public class DatabaseConnection {

    private final HikariDataSource dataSource;
    private final DatabaseProperties properties;

    public DatabaseConnection(DatabaseProperties properties) {
        this.properties = properties;
        this.dataSource = buildDataSource();
    }

    private HikariDataSource buildDataSource() {
        HikariConfig config = new HikariConfig();

        // Construcción de la URL JDBC usando las propiedades de Spring
        String jdbcUrl = String.format(
                "jdbc:sqlserver://%s;databaseName=%s;encrypt=true;trustServerCertificate=true",
                properties.getServer(),
                properties.getDatabases().getHonorarios());

        config.setJdbcUrl(jdbcUrl);
        config.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        config.setUsername(properties.getUsername());
        config.setPassword(properties.getPassword());

        // Pool tuning (Configuraciones de rendimiento)
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30_000);
        config.setIdleTimeout(600_000);
        config.setMaxLifetime(1_800_000);
        config.setLeakDetectionThreshold(5_000);
        config.setPoolName("SQLServer-Pool");

        AppLogger.logInfo("Iniciando Pool de conexiones Hikari para: " + properties.getServer());

        HikariDataSource ds = new HikariDataSource(config);

        // Verificación inicial de conexión
        try (Connection conn = ds.getConnection()) {
            AppLogger.logInfo("✅ Conexión a base de datos establecida correctamente.");
        } catch (SQLException e) {
            AppLogger.logError("❌ Error al establecer la conexión inicial: " + e.getMessage());
        }

        return ds;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @PreDestroy
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            AppLogger.logInfo("Pool de conexiones cerrado correctamente.");
        }
    }
}