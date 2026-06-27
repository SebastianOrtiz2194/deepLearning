package com.deeplearning.worker.engine;

import com.deeplearning.common.dto.PredictionResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StubInferenceEngineTest {

    private final StubInferenceEngine engine = new StubInferenceEngine();

    @Test
    @DisplayName("runInference returns deterministic stub results with 5 cat breed classifications")
    void runInferenceReturnsDeterministicResults() {
        PredictionResult result = engine.runInference("https://example.com/any-image.jpg");

        assertThat(result.imageUrl()).isEqualTo("https://example.com/any-image.jpg");
        assertThat(result.classifications()).hasSize(5);
        assertThat(result.classifications().get(0).label()).isEqualTo("Egyptian cat");
        assertThat(result.classifications().get(0).probability()).isEqualTo(0.92);
        assertThat(result.classifications().get(4).label()).isEqualTo("Siamese cat");
        assertThat(result.classifications().get(4).probability()).isEqualTo(0.32);
    }

    @Test
    @DisplayName("runInference results are sorted by descending probability")
    void classificationsAreSortedByProbability() {
        PredictionResult result = engine.runInference("https://example.com/any-image.jpg");

        for (int i = 0; i < result.classifications().size() - 1; i++) {
            assertThat(result.classifications().get(i).probability())
                    .isGreaterThanOrEqualTo(result.classifications().get(i + 1).probability());
        }
    }

    @Test
    @DisplayName("runInference returns same results for different image URLs")
    void returnsSameResultsRegardlessOfUrl() {
        PredictionResult r1 = engine.runInference("https://a.com/1.jpg");
        PredictionResult r2 = engine.runInference("https://b.com/2.png");

        assertThat(r1.classifications()).hasSize(r2.classifications().size());
        for (int i = 0; i < r1.classifications().size(); i++) {
            assertThat(r1.classifications().get(i).label())
                    .isEqualTo(r2.classifications().get(i).label());
        }
    }
}
