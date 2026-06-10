package com.deeplearning.inference.dto;

import com.deeplearning.common.dto.PredictionResult;
import com.deeplearning.common.enums.JobStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

/**
 * Response returned to the client for prediction job endpoints.
 *
 * <p>Provides the job identifier, current lifecycle status, an optional
 * human-readable message, and the inference result once the job completes.
 *
 * @param jobId   unique job identifier
 * @param status  current lifecycle status of the job
 * @param message human-readable description (e.g. "Job accepted")
 * @param result  classification predictions, available after completion
 * @since 1.0.0
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PredictionResponse(
        String jobId,
        JobStatus status,
        String message,
        PredictionResult result
) {
}
