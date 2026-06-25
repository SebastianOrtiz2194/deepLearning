package com.deeplearning.worker.engine;

import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.translator.ImageClassificationTranslator;
import ai.djl.translate.Translator;
import com.deeplearning.common.dto.PredictionResult;
import com.deeplearning.common.exception.InferenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

/**
 * Real DJL-based inference engine using a pre-trained ResNet-18 model.
 *
 * <p>The model is provided by {@link com.deeplearning.worker.config.DjlConfig}
 * as a Spring bean and injected via constructor. Each inference call creates
 * a new {@link Predictor} for thread safety.
 *
 * <p>Active by default; excluded when the {@code stub} Spring profile is active.
 *
 * @since 1.0.0
 */
@Component
@Profile("!stub")
public class DjlInferenceEngine implements InferenceEngine {

    private static final Logger log = LoggerFactory.getLogger(DjlInferenceEngine.class);

    private final ai.djl.Model model;

    /**
     * Creates the engine with the pre-loaded ResNet-18 model.
     *
     * @param model the DJL model bean provided by {@link com.deeplearning.worker.config.DjlConfig}
     */
    public DjlInferenceEngine(ai.djl.Model model) {
        this.model = model;
    }

    /**
     * Downloads the image from the given URL, runs it through ResNet-18,
     * and returns the top-5 classification labels with confidence scores.
     *
     * @param imageUrl publicly accessible URL of the image to classify
     * @return prediction result with top-5 classifications ordered by confidence
     * @throws InferenceException if the image cannot be fetched or inference fails
     */
    @Override
    public PredictionResult runInference(String imageUrl) {
        log.info("djl_inference_start imageUrl={}", imageUrl);

        Image image;
        try {
            image = loadImage(imageUrl);
        } catch (IOException e) {
            throw new InferenceException("Failed to download image from " + imageUrl, e);
        }

        Translator<Image, Classifications> translator =
                ImageClassificationTranslator.builder().build();

        try (Predictor<Image, Classifications> predictor = model.newPredictor(translator)) {
            Classifications result = predictor.predict(image);
            List<PredictionResult.Classification> classifications = result.topK(5).stream()
                    .map(c -> new PredictionResult.Classification(c.getClassName(), c.getProbability()))
                    .toList();

            log.info("djl_inference_complete imageUrl={} topLabel={} confidence={}",
                    imageUrl, classifications.get(0).label(), classifications.get(0).probability());

            return new PredictionResult(imageUrl, classifications);
        } catch (Exception e) {
            throw new InferenceException("Inference failed for image " + imageUrl, e);
        }
    }

    private Image loadImage(String imageUrl) throws IOException {
        URL url = new URL(imageUrl);
        try (InputStream in = url.openStream()) {
            return ImageFactory.getInstance().fromInputStream(in);
        }
    }
}
