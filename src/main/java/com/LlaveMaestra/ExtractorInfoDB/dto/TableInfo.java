package com.LlaveMaestra.ExtractorInfoDB.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TableInfo {
    private String name;
    private String catalog;
    private String schema;
    private String type;
}
