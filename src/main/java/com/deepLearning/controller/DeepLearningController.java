package com.deepLearning.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.UUID;

@RestController
@RequestMapping("/api/dl")
public class DeepLearningController {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final StringRedisTemplate redisTemplate;

    public DeepLearningController(KafkaTemplate<String, String> kafkaTemplate, StringRedisTemplate redisTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.redisTemplate = redisTemplate;
    }

    // 1. Enviar tarea de inferencia
    @PostMapping("/predict")
    public String startPrediction(@RequestBody String payload) {
        String jobId = UUID.randomUUID().toString();

        // Guardar estado inicial en Redis
        redisTemplate.opsForValue().set(jobId, "PROCESANDO");

        // Enviar a Kafka (El payload en un caso real sería un JSON con la URL de la imagen o los datos)
        String kafkaMessage = jobId + "|" + payload;
        kafkaTemplate.send("dl-tasks", kafkaMessage);

        return "Tarea recibida. ID de seguimiento: " + jobId;
    }

    // 2. Consultar resultado
    @GetMapping("/result/{jobId}")
    public String getResult(@PathVariable String jobId) {
        String result = redisTemplate.opsForValue().get(jobId);
        return result != null ? result : "Tarea no encontrada";
    }
}
