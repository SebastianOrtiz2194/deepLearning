package com.deeplearning.worker.consumer;

import com.deeplearning.common.dto.KafkaTask;
import com.deeplearning.common.dto.PredictionResult;
import com.deeplearning.common.enums.JobStatus;
import com.deeplearning.common.exception.InferenceException;
import com.deeplearning.worker.engine.InferenceEngine;
import com.deeplearning.worker.service.ResultPersistenceService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer that processes inference tasks asynchronously.
 *
 * <p>Listens on the configured topic with concurrent consumers for
 * parallel processing. Each message is deserialized, the inference
 * engine is invoked, and the result is persisted to Redis.
 *
 * @since 1.0.0
 */
@Component
public class InferenceConsumer {

    private static final Logger log = LoggerFactory.getLogger(InferenceConsumer.class);

    private final InferenceEngine inferenceEngine;
    private final ResultPersistenceService persistenceService;
    private final ObjectMapper objectMapper;

    /**
     * Creates the consumer with required dependencies.
     *
     * @param inferenceEngine    the inference engine to classify images
     * @param persistenceService the Redis-backed persistence layer
     * @param objectMapper       Jackson deserializer for Kafka messages
     */
    public InferenceConsumer(InferenceEngine inferenceEngine,
                             ResultPersistenceService persistenceService,
                             ObjectMapper objectMapper) {
        this.inferenceEngine = inferenceEngine;
        this.persistenceService = persistenceService;
        this.objectMapper = objectMapper;
    }

    /**
     * Consumes inference task messages from the Kafka topic.
     *
     * <p>Runs with 3 concurrent consumers for parallel processing.
     * The lifecycle for each message is:
     * <ol>
     *   <li>Deserialize the {@link KafkaTask} from JSON</li>
     *   <li>Update Redis status to {@link JobStatus#PROCESSING}</li>
     *   <li>Execute inference via {@link InferenceEngine#runInference}</li>
     *   <li>Persist the result with {@link JobStatus#COMPLETED}</li>
     * </ol>
     *
     * <p>On any failure the job status is set to {@link JobStatus#FAILED}.
     *
     * @param message the raw JSON message from Kafka
     */
    @KafkaListener(
            topics = "${app.kafka.topic.dl-tasks}",
            groupId = "dl-worker-group",
            concurrency = "3"
    )
    public void consume(String message) {
        KafkaTask task;
        try {
            task = objectMapper.readValue(message, KafkaTask.class);
        } catch (JsonProcessingException e) {
            log.error("kafka_deserialization_failed rawMessage={}", message, e);
            return;
        }

        String jobId = task.jobId();
        String imageUrl = task.imageUrl();
        log.info("kafka_consumed jobId={} imageUrl={}", jobId, imageUrl);

        try {
            persistenceService.saveStatus(jobId, JobStatus.PROCESSING);

            PredictionResult result = inferenceEngine.runInference(imageUrl);

            persistenceService.saveResult(jobId, result);
            log.info("job_completed jobId={}", jobId);
        } catch (InferenceException e) {
            log.error("inference_failed jobId={} imageUrl={}", jobId, imageUrl, e);
            persistenceService.saveError(jobId, e.getMessage());
            // TODO: Dead-letter queue — publish failed tasks to a DLQ topic for retry
        } catch (Exception e) {
            log.error("job_unexpected_error jobId={} imageUrl={}", jobId, imageUrl, e);
            persistenceService.saveError(jobId, "Unexpected error: " + e.getMessage());
        }
    }
}
