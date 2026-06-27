package com.deeplearning.inference;

import com.deeplearning.common.enums.JobStatus;
import com.deeplearning.inference.service.TaskResultService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@EmbeddedKafka(
        partitions = 1,
        topics = {"dl-tasks-topic"}
)
@ActiveProfiles("dev")
class EndToEndIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskResultService resultService;

    @Test
    @DisplayName("full flow: POST /predict → 202 with valid jobId, message is sent to embedded Kafka")
    void endToEndPredictProducesToKafka() throws Exception {
        mockMvc.perform(post("/api/v1/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"imageUrl":"https://example.com/test.jpg"}"""))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.jobId").isNotEmpty())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.message").isNotEmpty());

        verify(resultService).saveInitialState(any());
    }

    @Test
    @DisplayName("GET /api/v1/status/{jobId} → returns status from mocked Redis")
    void statusEndpointReturnsMockedStatus() throws Exception {
        String jobId = "test-job-123";
        when(resultService.getStatus(jobId)).thenReturn(JobStatus.PROCESSING);

        mockMvc.perform(get("/api/v1/status/{jobId}", jobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value(jobId))
                .andExpect(jsonPath("$.status").value("PROCESSING"));
    }
}
