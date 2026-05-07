package com.LlaveMaestra.ExtractorInfoDB.strategy.impl;

import com.LlaveMaestra.ExtractorInfoDB.dto.DatabaseConnectionDTO;
import com.LlaveMaestra.ExtractorInfoDB.strategy.DatabaseEngineStrategy;
import java.sql.Connection;
import java.sql.SQLException;

public class PostgresEngineStrategy implements DatabaseEngineStrategy {

    @Override
    public String buildJdbcUrl(DatabaseConnectionDTO config) {
        return String.format("jdbc:postgresql://%s:%d/%s",
                config.getHost(), config.getPort(), config.getDatabaseName());
    }

    @Override
    public String getDriverClass() {
        return "org.postgresql.Driver";
    }

    @Override
    public String resolveSchema(Connection conn, String schema) throws SQLException {
        if (schema != null && !schema.isEmpty()) return schema;
        return "public";
    }

    @Override
    public boolean supportsCatalogs() {
        return false; // Postgres usa esquemas dentro de una base de datos física
    }

    @Override
    public int getDefaultPort() {
        return 5432;
    }

    @Override
    public String getName() {
        return "PostgreSQL";
    }
}
