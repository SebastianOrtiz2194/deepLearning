package com.deeplearning.inference.service;

import com.deeplearning.common.dto.PredictionResult;
import com.deeplearning.common.enums.JobStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskResultServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private ObjectMapper objectMapper;

    private TaskResultService resultService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        resultService = new TaskResultService(redisTemplate, objectMapper);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("saveInitialState stores PENDING status with 24h TTL")
    void saveInitialStateStoresPending() {
        String jobId = "job-001";

        resultService.saveInitialState(jobId);

        verify(valueOperations).set(eq("dl:job:job-001:status"), eq("PENDING"), eq(Duration.ofHours(24)));
    }

    @Test
    @DisplayName("getStatus returns correct JobStatus for an existing key")
    void getStatusReturnsCorrectStatus() {
        String jobId = "job-001";
        when(valueOperations.get("dl:job:job-001:status")).thenReturn("PROCESSING");

        JobStatus status = resultService.getStatus(jobId);

        assertThat(status).isEqualTo(JobStatus.PROCESSING);
    }

    @Test
    @DisplayName("getStatus returns null when the key does not exist")
    void getStatusReturnsNullForMissingKey() {
        String jobId = "ghost";
        when(valueOperations.get("dl:job:ghost:status")).thenReturn(null);

        JobStatus status = resultService.getStatus(jobId);

        assertThat(status).isNull();
    }

    @Test
    @DisplayName("getResult returns deserialized PredictionResult")
    void getResultReturnsPredictionResult() throws JsonProcessingException {
        String jobId = "job-001";
        PredictionResult expected = new PredictionResult("https://example.com/cat.jpg",
                List.of(new PredictionResult.Classification("tabby cat", 0.92)));
        String json = objectMapper.writeValueAsString(expected);

        when(valueOperations.get("dl:job:job-001:result")).thenReturn(json);

        PredictionResult result = resultService.getResult(jobId);

        assertThat(result).isNotNull();
        assertThat(result.imageUrl()).isEqualTo("https://example.com/cat.jpg");
        assertThat(result.classifications()).hasSize(1);
        assertThat(result.classifications().get(0).label()).isEqualTo("tabby cat");
    }

    @Test
    @DisplayName("getResult returns null when the key does not exist")
    void getResultReturnsNullForMissingKey() {
        String jobId = "ghost";
        when(valueOperations.get("dl:job:ghost:result")).thenReturn(null);

        PredictionResult result = resultService.getResult(jobId);

        assertThat(result).isNull();
    }
}
