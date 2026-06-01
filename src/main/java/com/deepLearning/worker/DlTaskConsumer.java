package com.deepLearning.worker;

import com.deepLearning.dto.KafkaTask;
import com.deepLearning.service.DlResultService;
import com.deepLearning.worker.inference.DjlInferenceService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * "Worker" que escucha constantemente la cola de Kafka y procesa el Deep Learning.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DlTaskConsumer {

    private final DjlInferenceService inferenceService;
    private final DlResultService resultService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${app.kafka.topic.dl-tasks}", groupId = "dl-worker-group")
    public void consume(String message) {
        KafkaTask task;
        try {
            task = objectMapper.readValue(message, KafkaTask.class);
        } catch (JsonProcessingException e) {
            log.error("Error al deserializar mensaje de Kafka: {}", message, e);
            return;
        }

        String jobId = task.getJobId();
        String dataPayload = task.getDataPayload();

        log.info(">>> Worker consumiendo tarea Job: {}", jobId);

        try {
            // 1. Actualizar estado en Redis a "PROCESANDO"
            resultService.saveProcessingState(jobId);

            // 2. Ejecutar el modelo pesado (Bloqueante solo para este hilo/worker)
            String predictionOutput = inferenceService.runPrediction(dataPayload);

            // 3. Guardar el resultado final
            resultService.saveFinalResult(jobId, predictionOutput);

            log.info("<<< Tarea Job: {} completada exitosamente.", jobId);

        } catch (Exception e) {
            log.error("Error al procesar el Job: {}", jobId, e);
            resultService.saveErrorState(jobId, e.getMessage());
        }
    }
}
