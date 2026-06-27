package com.deeplearning.inference.service;

import com.deeplearning.common.dto.PredictionRequest;
import com.deeplearning.common.exception.TaskSubmissionException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskProducerServiceTest {

    private static final String TOPIC = "dl-tasks-topic";

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private ObjectMapper objectMapper;

    private TaskProducerService producerService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        producerService = new TaskProducerService(kafkaTemplate, objectMapper, TOPIC);
    }

    @Test
    @DisplayName("sendTaskToQueue sends serialized KafkaTask to the configured topic")
    void sendTaskToQueueSuccess() {
        String jobId = "job-001";
        PredictionRequest request = new PredictionRequest("https://example.com/cat.jpg", null);

        when(kafkaTemplate.send(eq(TOPIC), eq(jobId), anyString()))
                .thenReturn(CompletableFuture.completedFuture(mock(org.springframework.kafka.support.SendResult.class)));

        producerService.sendTaskToQueue(jobId, request);

        verify(kafkaTemplate).send(eq(TOPIC), eq(jobId), anyString());
    }

    @Test
    @DisplayName("sendTaskToQueue throws TaskSubmissionException on Kafka broker error")
    void sendTaskToQueueBrokerError() {
        String jobId = "job-002";
        PredictionRequest request = new PredictionRequest("https://example.com/dog.jpg", null);

        when(kafkaTemplate.send(eq(TOPIC), eq(jobId), anyString()))
                .thenReturn(CompletableFuture.failedFuture(
                        new ExecutionException(new RuntimeException("Broker unavailable"))));

        assertThatThrownBy(() -> producerService.sendTaskToQueue(jobId, request))
                .isInstanceOf(TaskSubmissionException.class)
                .hasMessageContaining("Failed to deliver message to Kafka");
    }
}
