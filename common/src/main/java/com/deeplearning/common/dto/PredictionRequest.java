package com.deeplearning.common.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Inbound request for a prediction inference job.
 *
 * <p>Clients provide an image URL that will be downloaded and classified
 * by the inference worker using a pre-trained deep learning model.
 *
 * @param imageUrl  publicly accessible URL of the image to classify
 * @param modelType optional model identifier when multiple models are available
 * @since 1.0.0
 */
public record PredictionRequest(

        @NotBlank(message = "imageUrl must not be blank")
        String imageUrl,

        String modelType
) {
}
