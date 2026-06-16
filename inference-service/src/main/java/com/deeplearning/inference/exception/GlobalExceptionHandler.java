package com.deeplearning.inference.exception;

import com.deeplearning.common.exception.TaskNotFoundException;
import com.deeplearning.common.exception.TaskSubmissionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

/**
 * Global exception handler that converts domain and validation exceptions
 * into consistent JSON error responses.
 *
 * @since 1.0.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles missing job IDs — the requested task does not exist in Redis.
     *
     * @param e the not-found exception with the offending job ID
     * @return 404 with error details
     */
    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(TaskNotFoundException e) {
        log.warn("task_not_found jobId={}", e.getJobId());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                        HttpStatus.NOT_FOUND.value(),
                        "Task not found",
                        e.getMessage(),
                        Instant.now()
                ));
    }

    /**
     * Handles Kafka connectivity or message delivery failures.
     *
     * @param e the submission exception from the producer service
     * @return 503 with error details
     */
    @ExceptionHandler(TaskSubmissionException.class)
    public ResponseEntity<ErrorResponse> handleSubmissionFailure(TaskSubmissionException e) {
        log.error("task_submission_failed", e);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse(
                        HttpStatus.SERVICE_UNAVAILABLE.value(),
                        "Service temporarily unavailable",
                        "The task could not be submitted. Please try again later.",
                        Instant.now()
                ));
    }

    /**
     * Handles request body validation errors and returns field-level details.
     *
     * @param e the validation exception with binding result
     * @return 400 with per-field error messages
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> new FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();

        log.warn("validation_failed errors={}", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "Validation failed",
                        "One or more fields are invalid",
                        Instant.now(),
                        fieldErrors
                ));
    }

    /**
     * Catch-all handler for unexpected errors. The original message is logged
     * but not exposed to the client.
     *
     * @param e the unhandled exception
     * @return 500 with a safe generic message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception e) {
        log.error("unexpected_error", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Internal server error",
                        "An unexpected error occurred. Please try again later.",
                        Instant.now()
                ));
    }

    /**
     * Standardized error response body.
     *
     * @param status    HTTP status code
     * @param error     short error type
     * @param message   human-readable detail
     * @param timestamp when the error occurred
     * @param fields    validation field errors (optional)
     */
    public record ErrorResponse(
            int status,
            String error,
            String message,
            Instant timestamp,
            List<FieldError> fields
    ) {
        public ErrorResponse(int status, String error, String message, Instant timestamp) {
            this(status, error, message, timestamp, null);
        }
    }

    /**
     * Represents a single field-level validation error.
     *
     * @param field   the field name that failed validation
     * @param message the constraint violation message
     */
    public record FieldError(String field, String message) {
    }
}
