package com.deeplearning.common.dto;

/**
 * Message payload sent through Kafka for inference task processing.
 *
 * <p>Produced by the inference-service when a prediction request is received,
 * and consumed by the inference-worker for actual model execution.
 *
 * @param jobId    unique identifier for tracking the job lifecycle
 * @param imageUrl publicly accessible URL of the image to classify
 * @since 1.0.0
 */
public record KafkaTask(
        String jobId,
        String imageUrl
) {
}
