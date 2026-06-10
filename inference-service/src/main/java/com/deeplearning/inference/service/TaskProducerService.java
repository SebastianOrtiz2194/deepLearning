package com.deeplearning.inference.service;

import com.deeplearning.common.dto.PredictionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Publishes inference tasks to the Kafka message queue.
 *
 * <p>Implementation will be completed in task 2.5.
 *
 * @since 1.0.0
 */
@Service
public class TaskProducerService {

    private static final Logger log = LoggerFactory.getLogger(TaskProducerService.class);

    /**
     * Serializes the prediction request and sends it to the configured Kafka topic.
     *
     * @param jobId   unique job identifier
     * @param request the prediction request payload
     */
    public void sendTaskToQueue(String jobId, PredictionRequest request) {
        // TODO: Implementation in task 2.5 — KafkaTemplate, ObjectMapper, proper error handling
        log.warn("kafka_producer_not_implemented jobId={}", jobId);
    }
}
