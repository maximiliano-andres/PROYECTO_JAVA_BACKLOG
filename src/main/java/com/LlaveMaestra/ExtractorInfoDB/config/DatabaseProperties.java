package com.LlaveMaestra.ExtractorInfoDB.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "db")
@Validated
@Getter
@ToString
public class DatabaseProperties {

    @ToString.Exclude
    @NotBlank(message = "El servidor de la base de datos no puede estar vacío")
    // agregar una validacion futura para host/ip y puerto
    private final String server;

    @ToString.Exclude
    @NotBlank(message = "El usuario de la base de datos no puede estar vacío")
    private final String username;

    @ToString.Exclude // ← Nunca en logs
    @NotBlank(message = "La contraseña de la base de datos no puede estar vacía")
    @Size(min = 8, message = "La contraseña debe tener mínimo 8 caracteres")
    private final String password;

    @ToString.Exclude
    @Valid
    @NotNull(message = "La sección db.databases es requerida")
    private final Databases databases;

    @ConstructorBinding
    public DatabaseProperties(String server, String username,
            String password, Databases databases) {
        this.server = server;
        this.username = username;
        this.password = password;
        this.databases = databases;
    }

    @Getter
    @ToString
    public static class Databases {

        @NotBlank(message = "La base de datos municipal no puede estar vacía")
        private final String municipal;

        @NotBlank(message = "La base de datos de honorarios no puede estar vacía")
        private final String honorarios;

        @NotBlank(message = "La base de datos mun-pae no puede estar vacía")
        private final String munPae;

        @NotBlank(message = "La base de datos renta-rrhh no puede estar vacía")
        private final String rentaRrhh;

        @ConstructorBinding
        public Databases(String municipal, String honorarios,
                String munPae, String rentaRrhh) {
            this.municipal = municipal;
            this.honorarios = honorarios;
            this.munPae = munPae;
            this.rentaRrhh = rentaRrhh;
        }
    }
}