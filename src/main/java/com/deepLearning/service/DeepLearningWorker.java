package com.deepLearning.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;

@Slf4j
@Service
public class DeepLearningWorker {

    private final StringRedisTemplate redisTemplate;
    private final DjlInferenceService djlService;

    public DeepLearningWorker(StringRedisTemplate redisTemplate, DjlInferenceService djlService) {
        this.redisTemplate = redisTemplate;
        this.djlService = djlService;
    }

    @KafkaListener(topics = "${app.kafka.topic.dl-tasks}", groupId = "dl-worker-group")
    public void consumeTask(String message) {
        try {
            // Parsear el mensaje
            String[] parts = message.split("\\|", 2);
            String jobId = parts[0];
            String data = parts[1];

            log.info("Procesando Job: {}", jobId);

            // Ejecutar inferencia de Deep Learning
            String predictionResult = djlService.runPrediction(data);

            // Guardar resultado final en Redis
            // El controlador espera que el estado sea "COMPLETADO" para retornar el resultado
            redisTemplate.opsForValue().set(jobId + ":status", "COMPLETADO");
            redisTemplate.opsForValue().set(jobId + ":result", predictionResult);

            log.info("Job {} completado exitosamente", jobId);

        } catch (Exception e) {
            log.error("Error procesando la tarea de Deep Learning", e);
        }
    }
}