package com.LlaveMaestra.ExtractorInfoDB.exception;

import com.LlaveMaestra.ExtractorInfoDB.dto.ConnectionStatusDTO;
import com.LlaveMaestra.ExtractorInfoDB.util.Wrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.sql.SQLException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<Wrapper<Void>> handleSQLException(SQLException e) {
        log.error("Error de base de datos: {}", e.getMessage());
        String message = translateError(e);
        return ResponseEntity.ok(Wrapper.error(message));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Wrapper<Void>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.ok(Wrapper.error(e.getMessage()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> handleNoResource(NoResourceFoundException e) {
        // No loguear como error favicon y otros recursos estáticos perdidos
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Wrapper<Void>> handleGeneralException(Exception e) {
        log.error("Error interno no controlado: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Wrapper.error("Error interno del servidor: " + e.getMessage()));
    }

    private String translateError(SQLException e) {
        String msg = e.getMessage().toLowerCase();
        if (msg.contains("login failed") || msg.contains("access denied") || msg.contains("authentication failed")) {
            return "Credenciales incorrectas o acceso denegado.";
        }
        if (msg.contains("network") || msg.contains("connection refused") || msg.contains("timed out")) {
            return "Servidor no disponible o tiempo de espera agotado.";
        }
        if (msg.contains("database") && msg.contains("does not exist")) {
            return "La base de datos especificada no existe.";
        }
        if (msg.contains("driver")) {
            return "Error en el driver de conexión. Contacte al administrador.";
        }
        return "Error de conexión: " + e.getMessage();
    }
}
