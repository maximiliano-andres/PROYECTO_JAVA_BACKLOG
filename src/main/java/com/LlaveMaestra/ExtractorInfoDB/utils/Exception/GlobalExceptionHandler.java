package com.LlaveMaestra.ExtractorInfoDB.utils.Exception;

// config/GlobalExceptionHandler.java

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Manejo centralizado de errores — nunca expone stacktraces al cliente.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataAccessException.class)
    public ProblemDetail handleDbError(DataAccessException ex) {
        log.error("Error de base de datos: {}", ex.getMessage(), ex);
        var detail = ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);
        detail.setTitle("Error de acceso a datos");
        detail.setDetail("No se pudo completar la consulta. Intente más tarde.");
        return detail;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleValidacion(IllegalArgumentException ex) {
        log.warn("Parámetro inválido: {}", ex.getMessage());
        var detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        detail.setTitle("Parámetro inválido");
        detail.setDetail(ex.getMessage());
        return detail;
    }
}