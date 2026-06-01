package com.deeplearning.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Structured result of a deep learning inference job.
 *
 * <p>Contains the top-N classification predictions with labels and
 * confidence probabilities, plus the original image URL for reference.
 *
 * @param imageUrl        the image that was classified
 * @param classifications ordered list of predictions (highest confidence first)
 * @since 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PredictionResult(
        String imageUrl,
        List<Classification> classifications
) {

    /**
     * A single classification prediction.
     *
     * @param label       human-readable class label (e.g. "tabby cat")
     * @param probability confidence score between 0.0 and 1.0
     */
    public record Classification(
            String label,
            double probability
    ) {
    }
}
