package com.deeplearning.common.enums;

/**
 * Represents the lifecycle status of an inference job.
 *
 * <p>Jobs transition through these statuses in order:
 * {@code PENDING → PROCESSING → COMPLETED} (or {@code FAILED}).
 *
 * @since 1.0.0
 */
public enum JobStatus {

    /** Job has been submitted and is waiting in the Kafka queue. */
    PENDING,

    /** Job has been picked up by a worker and inference is in progress. */
    PROCESSING,

    /** Inference completed successfully; results are available. */
    COMPLETED,

    /** Inference failed due to an error. */
    FAILED
}
