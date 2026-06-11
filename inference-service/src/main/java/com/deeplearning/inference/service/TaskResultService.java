package com.deeplearning.inference.service;

import com.deeplearning.common.dto.PredictionResult;
import com.deeplearning.common.enums.JobStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Manages inference job state and results stored in Redis.
 *
 * <p>Keys are namespaced under {@code dl:job:{jobId}} to avoid collision
 * with other Redis entries. All keys carry a 24-hour TTL so stale data
 * is cleaned up automatically.
 *
 * @since 1.0.0
 */
@Service
public class TaskResultService {

    private static final Logger log = LoggerFactory.getLogger(TaskResultService.class);

    private static final String KEY_PREFIX = "dl:job:";
    private static final String STATUS_SUFFIX = ":status";
    private static final String RESULT_SUFFIX = ":result";
    private static final Duration KEY_EXPIRATION = Duration.ofHours(24);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Creates the result service backed by Redis.
     *
     * @param redisTemplate auto-configured Redis client for string operations
     * @param objectMapper  Jackson serializer for result deserialization
     */
    public TaskResultService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
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
     * Saves the initial {@link JobStatus#PENDING} status for a newly created job.
     *
     * @param jobId unique job identifier
     */
    public void saveInitialState(String jobId) {
        log.info("redis_save_initial jobId={}", jobId);
        redisTemplate.opsForValue().set(statusKey(jobId), JobStatus.PENDING.name(), KEY_EXPIRATION);
    }

    /**
     * Retrieves the current lifecycle status of a job.
     *
     * @param jobId unique job identifier
     * @return the current job status, or {@code null} if the key does not exist
     */
    public JobStatus getStatus(String jobId) {
        String raw = redisTemplate.opsForValue().get(statusKey(jobId));
        if (raw == null) {
            return null;
        }
        try {
            return JobStatus.valueOf(raw);
        } catch (IllegalArgumentException e) {
            log.warn("redis_unknown_status jobId={} raw={}", jobId, raw);
            return null;
        }
    }

    /**
     * Retrieves the inference result for a completed job.
     *
     * @param jobId unique job identifier
     * @return the deserialized prediction result, or {@code null} if not yet stored
     */
    public PredictionResult getResult(String jobId) {
        String raw = redisTemplate.opsForValue().get(resultKey(jobId));
        if (raw == null) {
            return null;
        }
        try {
            return objectMapper.readValue(raw, PredictionResult.class);
        } catch (JsonProcessingException e) {
            log.warn("redis_deserialization_failed jobId={}", jobId, e);
            return null;
        }
    }
}
