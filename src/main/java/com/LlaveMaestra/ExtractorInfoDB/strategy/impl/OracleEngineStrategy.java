package com.LlaveMaestra.ExtractorInfoDB.strategy.impl;

import com.LlaveMaestra.ExtractorInfoDB.dto.DatabaseConnectionDTO;
import com.LlaveMaestra.ExtractorInfoDB.strategy.DatabaseEngineStrategy;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

public class OracleEngineStrategy implements DatabaseEngineStrategy {

    @Override
    public String buildJdbcUrl(DatabaseConnectionDTO config) {
        return String.format("jdbc:oracle:thin:@//%s:%d/%s",
                config.getHost(), config.getPort(), config.getDatabaseName());
    }

    @Override
    public String getDriverClass() {
        return "oracle.jdbc.OracleDriver";
    }

    @Override
    public String resolveSchema(Connection conn, String schema) throws SQLException {
        if (schema != null && !schema.isEmpty()) return schema;
        return conn.getMetaData().getUserName();
    }

    @Override
    public boolean supportsCatalogs() {
        return false;
    }

    @Override
    public int getDefaultPort() {
        return 1521;
    }

    @Override
    public String getName() {
        return "Oracle";
    }
}
