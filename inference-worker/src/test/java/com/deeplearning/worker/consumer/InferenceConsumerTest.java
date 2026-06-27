package com.deeplearning.worker.consumer;

import com.deeplearning.common.dto.PredictionResult;
import com.deeplearning.common.enums.JobStatus;
import com.deeplearning.common.exception.InferenceException;
import com.deeplearning.worker.engine.InferenceEngine;
import com.deeplearning.worker.service.ResultPersistenceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InferenceConsumerTest {

    private static final String VALID_MESSAGE = """
            {"jobId":"job-001","imageUrl":"https://example.com/cat.jpg"}""";

    @Mock
    private InferenceEngine inferenceEngine;

    @Mock
    private ResultPersistenceService persistenceService;

    private ObjectMapper objectMapper;

    private InferenceConsumer consumer;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        consumer = new InferenceConsumer(inferenceEngine, persistenceService, objectMapper);
    }

    @Test
    @DisplayName("consume processes valid message: sets PROCESSING, runs inference, saves result")
    void consumeProcessesValidMessage() {
        PredictionResult result = new PredictionResult("https://example.com/cat.jpg",
                List.of(new PredictionResult.Classification("tabby cat", 0.92)));
        when(inferenceEngine.runInference("https://example.com/cat.jpg")).thenReturn(result);

        consumer.consume(VALID_MESSAGE);

        verify(persistenceService).saveStatus("job-001", JobStatus.PROCESSING);
        verify(inferenceEngine).runInference("https://example.com/cat.jpg");
        verify(persistenceService).saveResult("job-001", result);
    }

    @Test
    @DisplayName("consume handles deserialization failure gracefully")
    void consumeHandlesDeserializationFailure() {
        consumer.consume("not valid json {{{");

        verify(persistenceService, never()).saveStatus(any(), any());
        verify(inferenceEngine, never()).runInference(any());
    }

    @Test
    @DisplayName("consume saves FAILED status and error on inference exception")
    void consumeHandlesInferenceError() {
        when(inferenceEngine.runInference("https://example.com/cat.jpg"))
                .thenThrow(new InferenceException("Model not loaded"));

        consumer.consume(VALID_MESSAGE);

        verify(persistenceService).saveStatus("job-001", JobStatus.PROCESSING);
        verify(persistenceService).saveError(eq("job-001"), any());
        verify(persistenceService, never()).saveResult(any(), any());
    }
}
