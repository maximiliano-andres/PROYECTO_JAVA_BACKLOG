package com.LlaveMaestra.ExtractorInfoDB.strategy;

import com.LlaveMaestra.ExtractorInfoDB.strategy.impl.MySqlEngineStrategy;
import com.LlaveMaestra.ExtractorInfoDB.strategy.impl.OracleEngineStrategy;
import com.LlaveMaestra.ExtractorInfoDB.strategy.impl.PostgresEngineStrategy;
import com.LlaveMaestra.ExtractorInfoDB.strategy.impl.SqlServerEngineStrategy;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class DatabaseEngineFactory {

    private final Map<String, DatabaseEngineStrategy> strategies = new HashMap<>();

    public DatabaseEngineFactory() {
        strategies.put("sqlserver", new SqlServerEngineStrategy());
        strategies.put("mysql", new MySqlEngineStrategy());
        strategies.put("postgresql", new PostgresEngineStrategy());
        strategies.put("oracle", new OracleEngineStrategy());
        // MariaDB puede usar el de MySQL inicialmente o uno específico si es necesario
        strategies.put("mariadb", new MySqlEngineStrategy()); 
    }

    public DatabaseEngineStrategy getStrategy(String engine) {
        DatabaseEngineStrategy strategy = strategies.get(engine.toLowerCase());
        if (strategy == null) {
            throw new IllegalArgumentException("Motor de base de datos no soportado: " + engine);
        }
        return strategy;
    }
}
