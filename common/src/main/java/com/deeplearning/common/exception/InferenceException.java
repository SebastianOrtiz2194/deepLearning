package com.deeplearning.common.exception;

/**
 * Thrown when the inference engine fails to process an image.
 *
 * <p>This could be caused by an invalid image URL, unsupported image format,
 * model loading failure, or an internal DJL error.
 *
 * @since 1.0.0
 */
public class InferenceException extends RuntimeException {

    /**
     * Creates a new exception with a descriptive message and root cause.
     *
     * @param message description of what went wrong
     * @param cause   the underlying exception
     */
    public InferenceException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new exception with a descriptive message.
     *
     * @param message description of what went wrong
     */
    public InferenceException(String message) {
        super(message);
    }
}
