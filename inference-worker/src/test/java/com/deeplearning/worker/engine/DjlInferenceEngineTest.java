package com.deeplearning.worker.engine;

import com.deeplearning.common.dto.PredictionResult;
import com.deeplearning.common.exception.InferenceException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration test for {@link DjlInferenceEngine} using a real ResNet-18 model.
 *
 * <p>This test requires:
 * <ul>
 *   <li>DJL PyTorch native runtime (~200 MB download on first run)</li>
 *   <li>ResNet-18 model weights from the DJL model zoo (~45 MB)</li>
 *   <li>Internet connectivity (to download the model and a test image)</li>
 * </ul>
 *
 * <p>Run manually with:
 * <pre>{@code
 * mvn test -pl inference-worker -Dtest=DjlInferenceEngineTest -Dspring.profiles.active=dev
 * }</pre>
 */
@SpringBootTest(classes = {com.deeplearning.worker.WorkerApplication.class})
@Disabled("Requires DJL native runtime and ResNet-18 model download. Run manually on a local dev machine.")
@Tag("integration")
class DjlInferenceEngineTest {

    @Autowired
    private DjlInferenceEngine engine;

    @Test
    @DisplayName("runInference with real ResNet-18 classifies a cat image with top-5 results")
    void runInferenceClassifiesCatImage() {
        PredictionResult result = engine.runInference(
                "https://upload.wikimedia.org/wikipedia/commons/4/4d/Cat_November_2010-1a.jpg");

        assertThat(result.imageUrl()).isNotNull();
        assertThat(result.classifications()).isNotEmpty();
        assertThat(result.classifications().size()).isLessThanOrEqualTo(5);

        result.classifications().forEach(c -> {
            assertThat(c.label()).isNotBlank();
            assertThat(c.probability()).isBetween(0.0, 1.0);
        });
    }

    @Test
    @DisplayName("runInference throws InferenceException for invalid image URL")
    void runInferenceThrowsOnInvalidUrl() {
        assertThatThrownBy(() -> engine.runInference("https://invalid.domain.doesnotexist/not-an-image"))
                .isInstanceOf(InferenceException.class);
    }
}
