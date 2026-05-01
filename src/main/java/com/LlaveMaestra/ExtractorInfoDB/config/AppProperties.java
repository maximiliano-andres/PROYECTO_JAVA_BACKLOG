package com.LlaveMaestra.ExtractorInfoDB.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Set;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    // Lista de bases de datos a excluir al obtener la lista de catálogos
    private Set<String> excludedDatabases;
}