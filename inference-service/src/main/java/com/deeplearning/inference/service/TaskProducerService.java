package com.deeplearning.inference.service;

import com.deeplearning.common.dto.KafkaTask;
import com.deeplearning.common.dto.PredictionRequest;
import com.deeplearning.common.exception.TaskSubmissionException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Publishes inference tasks to the Kafka message queue.
 *
 * <p>Serializes the prediction request into a {@link KafkaTask} message
 * and sends it to the configured topic. The send is confirmed synchronously
 * so that failures surface immediately to the controller.
 *
 * @since 1.0.0
 */
@Service
public class TaskProducerService {

    private static final Logger log = LoggerFactory.getLogger(TaskProducerService.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topicName;

    /**
     * Creates the producer service with the configured Kafka infrastructure.
     *
     * @param kafkaTemplate auto-configured Kafka template for sending messages
     * @param objectMapper  Jackson serializer for DTO-to-JSON conversion
     * @param topicName     destination topic from {@code app.kafka.topic.dl-tasks}
     */
    public TaskProducerService(KafkaTemplate<String, String> kafkaTemplate,
                               ObjectMapper objectMapper,
                               @Value("${app.kafka.topic.dl-tasks}") String topicName) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topicName = topicName;
    }

    /**
     * Serializes the prediction request and sends it to the configured Kafka topic.
     *
     * <p>The send future is awaited synchronously (5-second timeout) so that
     * any broker connectivity or serialization issues fail this call immediately
     * rather than silently dropping the message.
     *
     * @param jobId   unique job identifier
     * @param request the prediction request from the client
     * @throws TaskSubmissionException if serialization fails or the broker rejects the message
     */
    public void sendTaskToQueue(String jobId, PredictionRequest request) {
        KafkaTask task = new KafkaTask(jobId, request.imageUrl());

        String payload;
        try {
            payload = objectMapper.writeValueAsString(task);
        } catch (JsonProcessingException e) {
            log.error("kafka_serialization_failed jobId={}", jobId, e);
            throw new TaskSubmissionException("Failed to serialize Kafka message for job " + jobId, e);
        }

        log.info("kafka_sending jobId={} topic={}", jobId, topicName);
        try {
            kafkaTemplate.send(topicName, jobId, payload)
                    .get(5, TimeUnit.SECONDS);
            log.info("kafka_sent jobId={} topic={}", jobId, topicName);
        } catch (ExecutionException e) {
            log.error("kafka_broker_error jobId={}", jobId, e.getCause());
            throw new TaskSubmissionException("Failed to deliver message to Kafka for job " + jobId, e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("kafka_send_interrupted jobId={}", jobId);
            throw new TaskSubmissionException("Kafka send interrupted for job " + jobId, e);
        } catch (TimeoutException e) {
            log.error("kafka_send_timeout jobId={}", jobId);
            throw new TaskSubmissionException("Timed out waiting for Kafka acknowledgment for job " + jobId, e);
        }
    }
}
