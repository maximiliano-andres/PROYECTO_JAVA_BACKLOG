package com.LlaveMaestra.ExtractorInfoDB.utils;

import com.LlaveMaestra.ExtractorInfoDB.repository.HistoricoProyectoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Esta clase es un componente de Spring que se ejecuta automáticamente al arrancar la aplicación.
 * Es ideal para realizar validaciones iniciales o pruebas de conexión sin ensuciar la clase principal.
 * Al implementar CommandLineRunner, Spring llama a su método 'run' después de que el contexto esté listo.
 */
@Component
public class DatabaseStartupValidator implements CommandLineRunner {

    private final HistoricoProyectoRepository repository;

    public DatabaseStartupValidator(HistoricoProyectoRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        AppLogger.logInfo("=== INICIO DE VALIDACIÓN DE SISTEMA ===");
        
        try {
            AppLogger.logInfo("Probando conexión y consulta a la base de datos...");
            List<?> resultados = repository.findAll();

            if (resultados.isEmpty()) {
                AppLogger.logWarning("⚠️ La base de datos está conectada pero la tabla parece estar vacía.");
            } else {
                AppLogger.logInfo("✅ Conexión exitosa. Se encontraron " + resultados.size() + " registros.");
                AppLogger.logInfo("Muestra del primer registro: " + resultados.get(0));
            }

        } catch (Exception e) {
            AppLogger.logError("❌ Error crítico en el arranque: " + e.getMessage());
        }

        AppLogger.logInfo("=== FIN DE VALIDACIÓN DE SISTEMA ===");
    }
}
