package com.LlaveMaestra.ExtractorInfoDB.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConnectionStatusDTO {
    private boolean connected;
    private String engine;
    private String host;
    private String databaseName;
    private String username;
    private boolean adminMode;
    private String message;
}
