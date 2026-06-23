package com.deeplearning.worker.service;

import com.deeplearning.common.dto.PredictionResult;
import com.deeplearning.common.enums.JobStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Persists inference job status and results to Redis.
 *
 * <p>Keys are namespaced under {@code dl:job:{jobId}} to avoid collision
 * with other Redis entries. Status and result writes for terminal states
 * ({@code COMPLETED}, {@code FAILED}) are executed atomically via a Redis
 * pipeline. All keys carry a 24-hour TTL for automatic cleanup.
 *
 * @since 1.0.0
 */
@Service
public class ResultPersistenceService {

    private static final Logger log = LoggerFactory.getLogger(ResultPersistenceService.class);

    private static final String KEY_PREFIX = "dl:job:";
    private static final String STATUS_SUFFIX = ":status";
    private static final String RESULT_SUFFIX = ":result";
    private static final Duration KEY_EXPIRATION = Duration.ofHours(24);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Creates the persistence service backed by Redis.
     *
     * @param redisTemplate auto-configured Redis client for string operations
     * @param objectMapper  Jackson serializer for result serialization
     */
    public ResultPersistenceService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    private static String statusKey(String jobId) {
        return KEY_PREFIX + jobId + STATUS_SUFFIX;
    }

    private static String resultKey(String jobId) {
        return KEY_PREFIX + jobId + RESULT_SUFFIX;
    }

    /**
     * Saves an intermediate status update for a job.
     *
     * @param jobId  unique job identifier
     * @param status the new lifecycle status (e.g. {@code PROCESSING})
     */
    public void saveStatus(String jobId, JobStatus status) {
        log.info("redis_save_status jobId={} status={}", jobId, status);
        redisTemplate.opsForValue().set(statusKey(jobId), status.name(), KEY_EXPIRATION);
    }

    /**
     * Atomically saves the final status and inference result.
     *
     * <p>Both the {@code COMPLETED} status and the serialized result JSON
     * are written in a single Redis pipeline operation.
     *
     * @param jobId  unique job identifier
     * @param result the prediction result to serialize and store
     */
    @SuppressWarnings("unchecked")
    public void saveResult(String jobId, PredictionResult result) {
        log.info("redis_save_result jobId={}", jobId);
        try {
            String resultJson = objectMapper.writeValueAsString(result);
            redisTemplate.executePipelined(new SessionCallback<>() {
                @Override
                public Object execute(RedisOperations operations) {
                    operations.opsForValue().set(statusKey(jobId), JobStatus.COMPLETED.name(), KEY_EXPIRATION);
                    operations.opsForValue().set(resultKey(jobId), resultJson, KEY_EXPIRATION);
                    return null;
                }
            });
        } catch (JsonProcessingException e) {
            log.error("redis_serialization_failed jobId={}", jobId, e);
            saveError(jobId, "Failed to serialize inference result");
        }
    }

    /**
     * Atomically saves the {@code FAILED} status and error details.
     *
     * @param jobId        unique job identifier
     * @param errorMessage description of what went wrong
     */
    @SuppressWarnings("unchecked")
    public void saveError(String jobId, String errorMessage) {
        log.warn("redis_save_error jobId={} error={}", jobId, errorMessage);
        redisTemplate.executePipelined(new SessionCallback<>() {
            @Override
            public Object execute(RedisOperations operations) {
                operations.opsForValue().set(statusKey(jobId), JobStatus.FAILED.name(), KEY_EXPIRATION);
                operations.opsForValue().set(resultKey(jobId), errorMessage, KEY_EXPIRATION);
                return null;
            }
        });
    }
}
