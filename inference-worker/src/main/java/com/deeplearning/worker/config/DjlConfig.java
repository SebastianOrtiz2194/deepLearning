package com.deeplearning.worker.config;

import ai.djl.Application;
import ai.djl.MalformedModelException;
import ai.djl.Model;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.training.util.ProgressBar;
import com.deeplearning.common.exception.InferenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.IOException;

/**
 * DJL model configuration for the inference worker.
 *
 * <p>Provides a pre-trained ResNet-18 {@link Model} bean that is loaded
 * from the DJL PyTorch model zoo on startup. The model weights (~45 MB)
 * are downloaded automatically on first run and cached locally.
 *
 * <p>Only active when the {@code stub} profile is NOT active.
 *
 * @since 1.0.0
 */
@Configuration
@Profile("!stub")
public class DjlConfig {

    private static final Logger log = LoggerFactory.getLogger(DjlConfig.class);

    /**
     * Creates and loads the ResNet-18 image classification model.
     *
     * <p>The model is automatically closed when the Spring context shuts down
     * via the {@code destroyMethod = "close"} directive.
     *
     * @return a loaded ResNet-18 model ready for prediction
     * @throws InferenceException if the model cannot be found or downloaded
     */
    @Bean(destroyMethod = "close")
    public Model resnet18Model() {
        log.info("djl_loading_model model=ResNet-18");
        Criteria<Image, Classifications> criteria = Criteria.builder()
                .setTypes(Image.class, Classifications.class)
                .optApplication(Application.CV.IMAGE_CLASSIFICATION)
                .optProgress(new ProgressBar())
                .build();

        try {
            Model model = criteria.loadModel();
            log.info("djl_model_loaded model=ResNet-18");
            return model;
        } catch (ModelNotFoundException e) {
            throw new InferenceException("ResNet-18 model not found in DJL model zoo", e);
        } catch (MalformedModelException e) {
            throw new InferenceException("ResNet-18 model is malformed", e);
        } catch (IOException e) {
            throw new InferenceException("Failed to download ResNet-18 model", e);
        }
    }
}
