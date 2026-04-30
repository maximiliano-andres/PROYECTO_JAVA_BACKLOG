package com.LlaveMaestra.ExtractorInfoDB.util;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Getter;
import lombok.Setter;

@JsonPropertyOrder({"ok", "descripcion", "timestamp", "data" })
@Getter
@Setter
public class Wrapper<T> {

    private boolean ok;
    private String descripcion;
    private T data;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    public Wrapper(boolean ok, String descripcion, T data) {
        this.ok = ok;
        this.descripcion = descripcion;
        this.timestamp = LocalDateTime.now();
        this.data = data;
        
    }

    public static <T> Wrapper<T> success(String msg, T data) {
        return new Wrapper<>(true, msg, data);
    }

    public static <T> Wrapper<T> error(String msg) {
        return new Wrapper<>(false, msg, null);
    }
}