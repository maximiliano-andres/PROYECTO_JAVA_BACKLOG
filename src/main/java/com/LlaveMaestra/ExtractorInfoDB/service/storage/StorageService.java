package com.LlaveMaestra.ExtractorInfoDB.service.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;

@Service
@Slf4j
public class StorageService {

    private static final String TEMP_FOLDER_NAME = ".extractor_info_temp";

    /**
     * Encuentra el mejor directorio temporal basado en el espacio disponible en disco,
     * verificando permisos de escritura.
     */
    public Path getBestTempDirectory() {
        File[] roots = File.listRoots();
        
        // Ordenar discos por espacio usable de mayor a menor
        return Arrays.stream(roots)
            .sorted(Comparator.comparingLong(File::getUsableSpace).reversed())
            .map(root -> {
                try {
                    Path path = resolvePathForRoot(root);
                    if (!Files.exists(path)) {
                        Files.createDirectories(path);
                    }
                    // Verificar si podemos escribir realmente creando un archivo temporal de prueba
                    Path testFile = Files.createTempFile(path, "test_", ".tmp");
                    Files.delete(testFile);
                    
                    log.info("Directorio temporal seleccionado: {} ({} GB libres)", 
                        path, root.getUsableSpace() / (1024 * 1024 * 1024));
                    return path;
                } catch (Exception e) {
                    log.debug("Disco {} descartado por falta de permisos o error: {}", root.getPath(), e.getMessage());
                    return null;
                }
            })
            .filter(java.util.Objects::nonNull)
            .findFirst()
            .orElseGet(() -> {
                log.warn("No se encontró ningún disco externo con permisos. Usando carpeta del proyecto.");
                Path projectPath = Paths.get(System.getProperty("user.dir"), "temp_exports");
                try {
                    Files.createDirectories(projectPath);
                } catch (Exception e) {
                    log.error("¡Fallo crítico! Ni siquiera se puede escribir en la carpeta del proyecto.");
                }
                return projectPath;
            });
    }

    private Path resolvePathForRoot(File root) {
        String rootPath = root.getPath();
        if (rootPath.equals("/") || rootPath.contains(":\\")) {
            return Paths.get(rootPath, TEMP_FOLDER_NAME);
        }
        return Paths.get(rootPath, "temp_exports");
    }
}
