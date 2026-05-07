package com.LlaveMaestra.ExtractorInfoDB.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseConnectionDTO {

    @NotBlank(message = "El motor de base de datos es obligatorio")
    private String engine; // sqlserver, mysql, postgresql, oracle

    @NotBlank(message = "El host es obligatorio")
    private String host;

    @NotNull(message = "El puerto es obligatorio")
    private Integer port;

    @NotBlank(message = "El nombre de la base de datos es obligatorio")
    private String databaseName;

    @NotBlank(message = "El usuario es obligatorio")
    private String username;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    private boolean adminMode = false;
}
