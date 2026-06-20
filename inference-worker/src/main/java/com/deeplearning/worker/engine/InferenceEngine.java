package com.deeplearning.worker.engine;

import com.deeplearning.common.dto.PredictionResult;
import com.deeplearning.common.exception.InferenceException;

/**
 * Contract for a deep learning inference engine.
 *
 * <p>Implementations are responsible for downloading an image from a URL,
 * running it through a pre-trained model, and returning structured
 * classification predictions. This abstraction allows swapping between
 * a real DJL implementation and a lightweight stub for testing.
 *
 * @since 1.0.0
 */
public interface InferenceEngine {

    /**
     * Runs inference on the image at the given URL.
     *
     * @param imageUrl publicly accessible URL of the image to classify
     * @return prediction result with top-N classifications
     * @throws InferenceException if the image cannot be downloaded, the model
     *                            is not available, or inference fails
     */
    PredictionResult runInference(String imageUrl);
}
