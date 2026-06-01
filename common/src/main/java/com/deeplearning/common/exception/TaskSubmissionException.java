package com.deeplearning.common.exception;

/**
 * Thrown when a task submission to the message queue fails.
 *
 * <p>This typically indicates a Kafka connectivity issue or serialization error.
 * The caller should respond with an HTTP 503 (Service Unavailable).
 *
 * @since 1.0.0
 */
public class TaskSubmissionException extends RuntimeException {

    /**
     * Creates a new exception with a descriptive message and root cause.
     *
     * @param message description of what went wrong
     * @param cause   the underlying exception
     */
    public TaskSubmissionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new exception with a descriptive message.
     *
     * @param message description of what went wrong
     */
    public TaskSubmissionException(String message) {
        super(message);
    }
}
