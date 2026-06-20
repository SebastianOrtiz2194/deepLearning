package com.deeplearning.worker.engine;

import ai.djl.Application;
import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.repository.zoo.Criteria;
import ai.djl.modality.cv.translator.ImageClassificationTranslator;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.Translator;
import com.deeplearning.common.dto.PredictionResult;
import com.deeplearning.common.exception.InferenceException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
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
 * <p>Loads ResNet-18 from the DJL PyTorch model zoo on startup. The model
 * is downloaded automatically on first run and cached for subsequent starts.
 * Image preprocessing and top-5 classification extraction are handled by
 * DJL's built-in {@link ai.djl.modality.cv.translator.ImageClassificationTranslator}.
 *
 * <p>Active by default; excluded when the {@code stub} Spring profile is active.
 *
 * @since 1.0.0
 */
@Component
@Profile("!stub")
public class DjlInferenceEngine implements InferenceEngine {

    private static final Logger log = LoggerFactory.getLogger(DjlInferenceEngine.class);

    private ai.djl.Model model;

    /**
     * Loads the ResNet-18 model from the DJL model zoo.
     *
     * <p>On first startup the model weights are downloaded automatically
     * (~45 MB). Subsequent starts use the locally cached copy.
     */
    @PostConstruct
    public void init() {
        log.info("djl_loading_model model=ResNet-18");
        Criteria<Image, Classifications> criteria = Criteria.builder()
                .setTypes(Image.class, Classifications.class)
                .optApplication(Application.CV.IMAGE_CLASSIFICATION)
                .optProgress(new ProgressBar())
                .build();

        try {
            model = criteria.loadModel();
            log.info("djl_model_loaded model=ResNet-18");
        } catch (ModelNotFoundException e) {
            throw new InferenceException("ResNet-18 model not found in DJL model zoo", e);
        } catch (MalformedModelException e) {
            throw new InferenceException("ResNet-18 model is malformed", e);
        } catch (IOException e) {
            throw new InferenceException("Failed to download ResNet-18 model", e);
        }
    }

    /**
     * Closes the model and releases native resources.
     */
    @PreDestroy
    public void destroy() {
        if (model != null) {
            log.info("djl_closing_model");
            model.close();
        }
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
