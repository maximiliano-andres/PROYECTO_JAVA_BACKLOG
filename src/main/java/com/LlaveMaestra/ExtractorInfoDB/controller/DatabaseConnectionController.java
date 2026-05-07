package com.LlaveMaestra.ExtractorInfoDB.controller;

import com.LlaveMaestra.ExtractorInfoDB.dto.ConnectionStatusDTO;
import com.LlaveMaestra.ExtractorInfoDB.dto.DatabaseConnectionDTO;
import com.LlaveMaestra.ExtractorInfoDB.service.DatabaseConnectionService;
import com.LlaveMaestra.ExtractorInfoDB.util.Wrapper;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;

@RestController
@RequestMapping("/api/connection")
@RequiredArgsConstructor
@Slf4j
public class DatabaseConnectionController {

    private final DatabaseConnectionService connectionService;

    @PostMapping("/test")
    public ResponseEntity<Wrapper<String>> testConnection(@Valid @RequestBody DatabaseConnectionDTO config) throws SQLException {
        connectionService.testConnection(config);
        return ResponseEntity.ok(Wrapper.success("Conexión exitosa", null));
    }

    @PostMapping("/connect")
    public ResponseEntity<Wrapper<ConnectionStatusDTO>> connect(@Valid @RequestBody DatabaseConnectionDTO config, HttpSession session) throws SQLException {
        connectionService.connect(session.getId(), config);
        ConnectionStatusDTO status = ConnectionStatusDTO.builder()
                .connected(true)
                .engine(config.getEngine())
                .host(config.getHost())
                .databaseName(config.getDatabaseName())
                .username(config.getUsername())
                .adminMode(config.isAdminMode())
                .message("Conectado exitosamente")
                .build();
        return ResponseEntity.ok(Wrapper.success("Conectado exitosamente", status));
    }

    @PostMapping("/disconnect")
    public ResponseEntity<Void> disconnect(HttpSession session) {
        connectionService.disconnect(session.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/status")
    public ResponseEntity<Wrapper<ConnectionStatusDTO>> getStatus(HttpSession session) {
        DatabaseConnectionDTO config = connectionService.getConnectionConfig(session.getId());
        if (config != null) {
            ConnectionStatusDTO status = ConnectionStatusDTO.builder()
                    .connected(true)
                    .engine(config.getEngine())
                    .host(config.getHost())
                    .databaseName(config.getDatabaseName())
                    .username(config.getUsername())
                    .adminMode(config.isAdminMode())
                    .build();
            return ResponseEntity.ok(Wrapper.success("Sesión activa", status));
        }
        return ResponseEntity.ok(Wrapper.success("No hay sesión activa", ConnectionStatusDTO.builder().connected(false).build()));
    }
}
