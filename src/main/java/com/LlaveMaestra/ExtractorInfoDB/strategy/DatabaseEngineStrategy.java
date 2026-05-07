package com.LlaveMaestra.ExtractorInfoDB.strategy;

import com.LlaveMaestra.ExtractorInfoDB.dto.DatabaseConnectionDTO;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Interfaz estratégica para abstraer comportamientos específicos de cada motor de base de datos.
 */
public interface DatabaseEngineStrategy {
    
    /**
     * Construye la URL de conexión JDBC.
     */
    String buildJdbcUrl(DatabaseConnectionDTO config);

    /**
     * Retorna la clase del driver JDBC.
     */
    String getDriverClass();

    /**
     * Resuelve el esquema a utilizar basándose en la conexión.
     */
    String resolveSchema(Connection conn, String schema) throws SQLException;

    /**
     * Indica si el motor usa catálogos para listar bases de datos.
     */
    boolean supportsCatalogs();

    /**
     * Puerto por defecto del motor.
     */
    int getDefaultPort();
    
    /**
     * Nombre amigable del motor.
     */
    String getName();
}
