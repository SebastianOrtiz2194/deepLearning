package com.deeplearning.inference.service;

import com.deeplearning.common.dto.PredictionResult;
import com.deeplearning.common.enums.JobStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Manages inference job state and results in Redis.
 *
 * <p>Implementation will be completed in task 2.6.
 *
 * @since 1.0.0
 */
@Service
public class TaskResultService {

    private static final Logger log = LoggerFactory.getLogger(TaskResultService.class);

    /**
     * Saves the initial PENDING status for a newly created job.
     *
     * @param jobId unique job identifier
     */
    public void saveInitialState(String jobId) {
        // TODO: Implementation in task 2.6 — Redis pipeline, namespaced keys
        log.warn("redis_save_not_implemented jobId={}", jobId);
    }

    /**
     * Retrieves the current status of a job from Redis.
     *
     * @param jobId unique job identifier
     * @return the current job status, or {@code null} if not found
     */
    public JobStatus getStatus(String jobId) {
        // TODO: Implementation in task 2.6 — StringRedisTemplate read
        log.warn("redis_read_not_implemented jobId={}", jobId);
        return null;
    }

    /**
     * Retrieves the inference result for a completed job from Redis.
     *
     * @param jobId unique job identifier
     * @return the prediction result, or {@code null} if not yet available
     */
    public PredictionResult getResult(String jobId) {
        // TODO: Implementation in task 2.6 — StringRedisTemplate read + deserialize
        log.warn("redis_read_not_implemented jobId={}", jobId);
        return null;
    }
}
