package com.LlaveMaestra.ExtractorInfoDB.service.export;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class ExportProgressService {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter createEmitter(String jobId) {
        SseEmitter emitter = new SseEmitter(600_000L); // 10 minutos de timeout
        emitters.put(jobId, emitter);

        emitter.onCompletion(() -> emitters.remove(jobId));
        emitter.onTimeout(() -> emitters.remove(jobId));
        emitter.onError((e) -> emitters.remove(jobId));

        return emitter;
    }

    public void publishProgress(String jobId, String message) {
        if (jobId == null) return;
        
        SseEmitter emitter = emitters.get(jobId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("progress")
                        .data(message));
            } catch (IOException e) {
                log.warn("Error enviando progreso para jobId {}: {}", jobId, e.getMessage());
                emitters.remove(jobId);
            }
        }
    }

    public void completeProgress(String jobId) {
        if (jobId == null) return;
        
        SseEmitter emitter = emitters.get(jobId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("complete")
                        .data("Finalizado"));
                emitter.complete();
            } catch (IOException e) {
                log.warn("Error completando progreso para jobId {}: {}", jobId, e.getMessage());
            } finally {
                emitters.remove(jobId);
            }
        }
    }
}
