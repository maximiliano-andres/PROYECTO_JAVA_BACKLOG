package com.LlaveMaestra.ExtractorInfoDB.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * Implementación de AbstractRoutingDataSource que selecciona el DataSource
 * basándose en el valor almacenado en DynamicDataSourceContextHolder.
 */
public class DynamicRoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        return DynamicDataSourceContextHolder.getDataSourceKey();
    }
}
