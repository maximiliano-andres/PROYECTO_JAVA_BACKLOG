package com.LlaveMaestra.ExtractorInfoDB.service;

import com.LlaveMaestra.ExtractorInfoDB.config.DynamicRoutingDataSource;
import com.LlaveMaestra.ExtractorInfoDB.dto.DatabaseConnectionDTO;
import com.LlaveMaestra.ExtractorInfoDB.strategy.DatabaseEngineFactory;
import com.LlaveMaestra.ExtractorInfoDB.strategy.DatabaseEngineStrategy;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class DatabaseConnectionService {

    private final DynamicRoutingDataSource dynamicRoutingDataSource;
    private final DatabaseEngineFactory engineFactory;
    private final Map<Object, Object> targetDataSources = new ConcurrentHashMap<>();
    private final Map<String, DatabaseConnectionDTO> connectionConfigs = new ConcurrentHashMap<>();

    public DatabaseConnectionService(DynamicRoutingDataSource dynamicRoutingDataSource, DatabaseEngineFactory engineFactory) {
        this.dynamicRoutingDataSource = dynamicRoutingDataSource;
        this.engineFactory = engineFactory;
    }

    public void testConnection(DatabaseConnectionDTO config) throws SQLException {
        DatabaseEngineStrategy strategy = engineFactory.getStrategy(config.getEngine());
        String url = strategy.buildJdbcUrl(config);
        String driverClass = strategy.getDriverClass();

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setPassword(config.getPassword());
        hikariConfig.setDriverClassName(driverClass);
        hikariConfig.setInitializationFailTimeout(5000);
        hikariConfig.setConnectionTimeout(5000);
        hikariConfig.setMaximumPoolSize(1);

        try (HikariDataSource ds = new HikariDataSource(hikariConfig)) {
            try (Connection conn = ds.getConnection()) {
                if (!conn.isValid(5)) {
                    throw new SQLException("La conexión se estableció pero no es válida.");
                }
            }
        } catch (SQLException e) {
            log.error("Error al probar la conexión: {}", e.getMessage(), e);
            throw e;
        }
    }

    public void connect(String sessionId, DatabaseConnectionDTO config) throws SQLException {
        if (targetDataSources.containsKey(sessionId)) {
            disconnect(sessionId);
        }

        DatabaseEngineStrategy strategy = engineFactory.getStrategy(config.getEngine());
        String url = strategy.buildJdbcUrl(config);
        String driverClass = strategy.getDriverClass();

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setPassword(config.getPassword());
        hikariConfig.setDriverClassName(driverClass);
        hikariConfig.setPoolName("Pool-" + sessionId);
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setMinimumIdle(2);

        HikariDataSource dataSource = new HikariDataSource(hikariConfig);
        
        // Verificar conexión antes de agregarla
        try (Connection conn = dataSource.getConnection()) {
            log.info("Conexión exitosa para sesión: {}. Motor: {}. Modo Admin: {}", 
                sessionId, strategy.getName(), config.isAdminMode());
        } catch (SQLException e) {
            dataSource.close();
            throw e;
        }

        targetDataSources.put(sessionId, dataSource);
        connectionConfigs.put(sessionId, config);
        
        refreshDataSources();
    }

    public void disconnect(String sessionId) {
        HikariDataSource ds = (HikariDataSource) targetDataSources.remove(sessionId);
        if (ds != null) {
            ds.close();
            log.info("Conexión cerrada para sesión: {}", sessionId);
        }
        connectionConfigs.remove(sessionId);
        refreshDataSources();
    }

    public DatabaseConnectionDTO getConnectionConfig(String sessionId) {
        return connectionConfigs.get(sessionId);
    }

    public DatabaseEngineStrategy getStrategyForSession(String sessionId) {
        DatabaseConnectionDTO config = getConnectionConfig(sessionId);
        return config != null ? engineFactory.getStrategy(config.getEngine()) : null;
    }

    private void refreshDataSources() {
        dynamicRoutingDataSource.setTargetDataSources(targetDataSources);
        dynamicRoutingDataSource.afterPropertiesSet();
    }

}
