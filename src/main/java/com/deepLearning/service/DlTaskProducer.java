package com.deepLearning.service;

import com.deepLearning.dto.PredictionRequest;
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

    @Value("${app.kafka.topic.dl-tasks}")
    private String topicName;

    public void sendTaskToQueue(String jobId, PredictionRequest request) {
        // En un proyecto real avanzado, podrías serializar el objeto a JSON.
        // Aquí concatenamos de forma sencilla: "jobId|dataPayload"
        String payload = jobId + "|" + request.getDataPayload();

        log.info("Enviando tarea a Kafka. Job: {}, Tópico: {}", jobId, topicName);
        kafkaTemplate.send(topicName, jobId, payload);
    }
}