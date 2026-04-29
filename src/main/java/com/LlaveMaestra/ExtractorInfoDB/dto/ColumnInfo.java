package com.LlaveMaestra.ExtractorInfoDB.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ColumnInfo {
    private String name;
    private String type;
    private int size;
    private boolean nullable;
    private int position;
}
