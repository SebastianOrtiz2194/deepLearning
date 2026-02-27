package com.deepLearning.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;

@Service
public class DeepLearningWorker {

    private final StringRedisTemplate redisTemplate;
    private final DjlInferenceService djlService;

    public DeepLearningWorker(StringRedisTemplate redisTemplate, DjlInferenceService djlService) {
        this.redisTemplate = redisTemplate;
        this.djlService = djlService;
    }

    @KafkaListener(topics = "dl-tasks", groupId = "dl-worker-group")
    public void consumeTask(String message) {
        try {
            // Parsear el mensaje
            String[] parts = message.split("\\|", 2);
            String jobId = parts[0];
            String data = parts[1];

            System.out.println("Procesando Job: " + jobId);

            // Ejecutar inferencia de Deep Learning
            String predictionResult = djlService.predict(data);

            // Guardar resultado final en Redis
            redisTemplate.opsForValue().set(jobId, "COMPLETADO: " + predictionResult);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}