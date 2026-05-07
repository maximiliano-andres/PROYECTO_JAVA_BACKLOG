package com.LlaveMaestra.ExtractorInfoDB.strategy.impl;

import com.LlaveMaestra.ExtractorInfoDB.dto.DatabaseConnectionDTO;
import com.LlaveMaestra.ExtractorInfoDB.strategy.DatabaseEngineStrategy;
import java.sql.Connection;
import java.sql.SQLException;

public class SqlServerEngineStrategy implements DatabaseEngineStrategy {

    @Override
    public String buildJdbcUrl(DatabaseConnectionDTO config) {
        String host = config.getHost();
        if (host.contains("\\")) {
            String[] parts = host.split("\\\\", 2);
            return String.format("jdbc:sqlserver://%s;instanceName=%s;databaseName=%s;encrypt=true;trustServerCertificate=true",
                    parts[0], parts[1], config.getDatabaseName());
        }
        return String.format("jdbc:sqlserver://%s:%d;databaseName=%s;encrypt=true;trustServerCertificate=true",
                host, config.getPort(), config.getDatabaseName());
    }

    @Override
    public String getDriverClass() {
        return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    }

    @Override
    public String resolveSchema(Connection conn, String schema) throws SQLException {
        if (schema != null && !schema.isEmpty()) return schema;
        return "dbo";
    }

    @Override
    public boolean supportsCatalogs() {
        return true;
    }

    @Override
    public int getDefaultPort() {
        return 1433;
    }

    @Override
    public String getName() {
        return "SQL Server";
    }
}
