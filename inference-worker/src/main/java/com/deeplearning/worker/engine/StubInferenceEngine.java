package com.deeplearning.worker.engine;

import com.deeplearning.common.dto.PredictionResult;
import com.deeplearning.common.dto.PredictionResult.Classification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Stub inference engine that returns deterministic fake results.
 *
 * <p>Active only when the {@code stub} Spring profile is active.
 * Useful for local development and testing without requiring the
 * DJL native PyTorch runtime or model download.
 *
 * @since 1.0.0
 */
@Component
@Profile("stub")
public class StubInferenceEngine implements InferenceEngine {

    private static final Logger log = LoggerFactory.getLogger(StubInferenceEngine.class);

    private static final List<Classification> STUB_RESULTS = List.of(
            new Classification("Egyptian cat", 0.92),
            new Classification("tabby cat", 0.85),
            new Classification("tiger cat", 0.73),
            new Classification("lynx", 0.45),
            new Classification("Siamese cat", 0.32)
    );

    /**
     * Returns a deterministic set of fake classification results.
     *
     * @param imageUrl the image URL (ignored in stub mode)
     * @return prediction result with pre-defined cat breed classifications
     */
    @Override
    public PredictionResult runInference(String imageUrl) {
        log.info("stub_inference imageUrl={}", imageUrl);
        return new PredictionResult(imageUrl, STUB_RESULTS);
    }
}
