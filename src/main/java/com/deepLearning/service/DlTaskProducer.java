package com.deepLearning.service;

import com.deepLearning.dto.KafkaTask;
import com.deepLearning.dto.PredictionRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Servicio para enviar eventos/tareas a la cola de Kafka.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DlTaskProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topic.dl-tasks}")
    private String topicName;

    public void sendTaskToQueue(String jobId, PredictionRequest request) {
        KafkaTask task = KafkaTask.builder()
                .jobId(jobId)
                .dataPayload(request.getDataPayload())
                .build();

        try {
            String payload = objectMapper.writeValueAsString(task);
            log.info("Enviando tarea a Kafka. Job: {}, Tópico: {}", jobId, topicName);
            kafkaTemplate.send(topicName, jobId, payload);
        } catch (JsonProcessingException e) {
            log.error("Error al serializar la tarea para el Job: {}", jobId, e);
            throw new RuntimeException("Error al serializar mensaje de Kafka", e);
        }
    }
}
