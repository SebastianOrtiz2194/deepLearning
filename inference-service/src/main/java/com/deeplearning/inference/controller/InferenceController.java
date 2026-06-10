package com.deeplearning.inference.controller;

import com.deeplearning.common.dto.PredictionRequest;
import com.deeplearning.common.dto.PredictionResult;
import com.deeplearning.common.enums.JobStatus;
import com.deeplearning.common.exception.TaskNotFoundException;
import com.deeplearning.inference.dto.PredictionResponse;
import com.deeplearning.inference.service.TaskProducerService;
import com.deeplearning.inference.service.TaskResultService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller for the inference API.
 *
 * <p>Accepts prediction requests, enqueues them for asynchronous
 * processing, and provides endpoints to query job status and results.
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1")
public class InferenceController {

    private static final Logger log = LoggerFactory.getLogger(InferenceController.class);

    private final TaskProducerService taskProducer;
    private final TaskResultService resultService;

    /**
     * Creates the controller with required dependencies.
     *
     * @param taskProducer responsible for enqueueing tasks via Kafka
     * @param resultService responsible for reading job state from Redis
     */
    public InferenceController(TaskProducerService taskProducer, TaskResultService resultService) {
        this.taskProducer = taskProducer;
        this.resultService = resultService;
    }

    /**
     * Submits a new image classification job for asynchronous processing.
     *
     * <p>The job is enqueued to Kafka and the client receives an immediate
     * response with a job ID to poll for status and results.
     *
     * @param request the prediction request containing the image URL
     * @return 202 Accepted with job ID and PENDING status
     */
    @PostMapping("/predict")
    public ResponseEntity<PredictionResponse> startPrediction(
            @Valid @RequestBody PredictionRequest request) {
        String jobId = UUID.randomUUID().toString();
        log.info("prediction_requested jobId={} imageUrl={}", jobId, request.imageUrl());

        taskProducer.sendTaskToQueue(jobId, request);
        resultService.saveInitialState(jobId);

        PredictionResponse response = PredictionResponse.builder()
                .jobId(jobId)
                .status(JobStatus.PENDING)
                .message("Job accepted and queued for processing")
                .build();

        return ResponseEntity.accepted().body(response);
    }

    /**
     * Queries the current status of an inference job.
     *
     * @param jobId the job identifier returned by {@code POST /predict}
     * @return 200 with current status, or 404 if the job is unknown
     */
    @GetMapping("/status/{jobId}")
    public ResponseEntity<PredictionResponse> getStatus(@PathVariable String jobId) {
        JobStatus status = resultService.getStatus(jobId);
        if (status == null) {
            throw new TaskNotFoundException(jobId);
        }

        PredictionResponse response = PredictionResponse.builder()
                .jobId(jobId)
                .status(status)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the inference result for a completed job.
     *
     * @param jobId the job identifier returned by {@code POST /predict}
     * @return 200 with classification results, or 404 if the job is unknown
     */
    @GetMapping("/result/{jobId}")
    public ResponseEntity<PredictionResponse> getResult(@PathVariable String jobId) {
        JobStatus status = resultService.getStatus(jobId);
        if (status == null) {
            throw new TaskNotFoundException(jobId);
        }

        PredictionResponse.PredictionResponseBuilder builder = PredictionResponse.builder()
                .jobId(jobId)
                .status(status);

        if (status == JobStatus.COMPLETED || status == JobStatus.FAILED) {
            PredictionResult result = resultService.getResult(jobId);
            builder.result(result);
        }

        return ResponseEntity.ok(builder.build());
    }
}
