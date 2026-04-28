package com.LlaveMaestra.ExtractorInfoDB;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Punto de entrada principal de la aplicación Spring Boot.
 * La anotación @SpringBootApplication activa la configuración automática,
 * el escaneo de componentes y permite definir configuraciones adicionales.
 */
@SpringBootApplication
@ConfigurationPropertiesScan // Escanea @ConfigurationProperties en todos los paquetes
public class ExtractorInfoDbApplication {

    public static void main(String[] args) {
        // Arranca la aplicación Spring, inicializando el servidor Tomcat y el contexto
        // de Spring.
        SpringApplication.run(ExtractorInfoDbApplication.class, args);
    }

}
