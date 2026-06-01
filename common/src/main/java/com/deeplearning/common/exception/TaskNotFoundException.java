package com.deeplearning.common.exception;

/**
 * Thrown when a requested inference task cannot be found.
 *
 * <p>This typically means the job ID does not exist in Redis,
 * either because it was never created or has expired.
 * The caller should respond with an HTTP 404 (Not Found).
 *
 * @since 1.0.0
 */
public class TaskNotFoundException extends RuntimeException {

    private final String jobId;

    /**
     * Creates a new exception for the given job ID.
     *
     * @param jobId the job ID that was not found
     */
    public TaskNotFoundException(String jobId) {
        super("Inference task not found: " + jobId);
        this.jobId = jobId;
    }

    /**
     * Returns the job ID that was not found.
     *
     * @return the missing job ID
     */
    public String getJobId() {
        return jobId;
    }
}
