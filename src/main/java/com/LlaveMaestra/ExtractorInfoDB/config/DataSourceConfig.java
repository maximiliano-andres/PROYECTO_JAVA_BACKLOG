package com.LlaveMaestra.ExtractorInfoDB.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DataSourceConfig {


    @Bean
    public DataSource defaultDataSource(DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Bean
    @Primary
    public DynamicRoutingDataSource dataSource(DataSource defaultDataSource) {
        DynamicRoutingDataSource routingDataSource = new DynamicRoutingDataSource();
        routingDataSource.setDefaultTargetDataSource(defaultDataSource);
        
        Map<Object, Object> targetDataSources = new HashMap<>();
        // Inicialmente solo el default
        routingDataSource.setTargetDataSources(targetDataSources);
        
        return routingDataSource;
    }
}
