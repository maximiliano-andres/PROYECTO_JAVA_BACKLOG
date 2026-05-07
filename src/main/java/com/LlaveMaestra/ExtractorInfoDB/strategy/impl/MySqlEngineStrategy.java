package com.LlaveMaestra.ExtractorInfoDB.strategy.impl;

import com.LlaveMaestra.ExtractorInfoDB.dto.DatabaseConnectionDTO;
import com.LlaveMaestra.ExtractorInfoDB.strategy.DatabaseEngineStrategy;
import java.sql.Connection;
import java.sql.SQLException;

public class MySqlEngineStrategy implements DatabaseEngineStrategy {

    @Override
    public String buildJdbcUrl(DatabaseConnectionDTO config) {
        return String.format("jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
                config.getHost(), config.getPort(), config.getDatabaseName());
    }

    @Override
    public String getDriverClass() {
        return "com.mysql.cj.jdbc.Driver";
    }

    @Override
    public String resolveSchema(Connection conn, String schema) throws SQLException {
        return null; // MySQL usa catálogos como bases de datos, no tiene esquemas intermedios como SQL Server/Postgres
    }

    @Override
    public boolean supportsCatalogs() {
        return true;
    }

    @Override
    public int getDefaultPort() {
        return 3306;
    }

    @Override
    public String getName() {
        return "MySQL";
    }
}
