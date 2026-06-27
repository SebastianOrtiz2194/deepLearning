package com.deeplearning.inference.controller;

import com.deeplearning.common.dto.PredictionRequest;
import com.deeplearning.common.dto.PredictionResult;
import com.deeplearning.common.enums.JobStatus;
import com.deeplearning.inference.service.TaskProducerService;
import com.deeplearning.inference.service.TaskResultService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InferenceController.class)
class InferenceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskProducerService taskProducer;

    @MockBean
    private TaskResultService resultService;

    @Test
    @DisplayName("POST /api/v1/predict valid request → 202 with jobId and PENDING status")
    void predictValidRequestReturns202() throws Exception {
        mockMvc.perform(post("/api/v1/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"imageUrl":"https://example.com/cat.jpg"}"""))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.jobId").isNotEmpty())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.message").isNotEmpty());

        verify(taskProducer).sendTaskToQueue(any(), any(PredictionRequest.class));
        verify(resultService).saveInitialState(any());
    }

    @Test
    @DisplayName("POST /api/v1/predict blank imageUrl → 400 with validation errors")
    void predictBlankImageUrlReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"imageUrl":""}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation failed"));

        verify(taskProducer, never()).sendTaskToQueue(any(), any());
    }

    @Test
    @DisplayName("GET /api/v1/status/{jobId} valid job → 200 with status")
    void getStatusValidJobReturns200() throws Exception {
        String jobId = "abc-123";
        when(resultService.getStatus(jobId)).thenReturn(JobStatus.PROCESSING);

        mockMvc.perform(get("/api/v1/status/{jobId}", jobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value(jobId))
                .andExpect(jsonPath("$.status").value("PROCESSING"));
    }

    @Test
    @DisplayName("GET /api/v1/status/{jobId} unknown job → 404")
    void getStatusUnknownJobReturns404() throws Exception {
        String jobId = "unknown-456";
        when(resultService.getStatus(jobId)).thenReturn(null);

        mockMvc.perform(get("/api/v1/status/{jobId}", jobId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Task not found"));
    }

    @Test
    @DisplayName("GET /api/v1/result/{jobId} completed job → 200 with result")
    void getResultCompletedJobReturns200() throws Exception {
        String jobId = "done-789";
        PredictionResult result = new PredictionResult("https://example.com/cat.jpg",
                List.of(new PredictionResult.Classification("tabby cat", 0.92)));

        when(resultService.getStatus(jobId)).thenReturn(JobStatus.COMPLETED);
        when(resultService.getResult(jobId)).thenReturn(result);

        mockMvc.perform(get("/api/v1/result/{jobId}", jobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value(jobId))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.result.classifications[0].label").value("tabby cat"))
                .andExpect(jsonPath("$.result.classifications[0].probability").value(0.92));
    }

    @Test
    @DisplayName("GET /api/v1/result/{jobId} unknown job → 404")
    void getResultUnknownJobReturns404() throws Exception {
        String jobId = "ghost-000";
        when(resultService.getStatus(jobId)).thenReturn(null);

        mockMvc.perform(get("/api/v1/result/{jobId}", jobId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
