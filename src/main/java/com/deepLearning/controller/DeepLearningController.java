package com.deepLearning.controller;

import com.deepLearning.dto.PredictionRequest;
import com.deepLearning.dto.PredictionResponse;
import com.deepLearning.enums.JobStatus;
import com.deepLearning.service.DlResultService;
import com.deepLearning.service.DlTaskProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * API Gateway para recibir solicitudes HTTP de clientes web/móviles.
 */
@RestController
@RequestMapping("/api/dl")
@RequiredArgsConstructor
public class DeepLearningController {

    private final DlTaskProducer taskProducer;
    private final DlResultService resultService;

    @PostMapping("/predict")
    public ResponseEntity<PredictionResponse> startPrediction(@RequestBody PredictionRequest request) {
        // 1. Generar un ID único para la solicitud
        String jobId = UUID.randomUUID().toString();

        // 2. Guardar estado en Redis (Caché)
        resultService.saveInitialState(jobId);

        // 3. Mandar el evento asíncrono a Kafka
        taskProducer.sendTaskToQueue(jobId, request);

        // 4. Responder al cliente inmediatamente sin bloquear
        PredictionResponse response = PredictionResponse.builder()
                .jobId(jobId)
                .status(JobStatus.PENDIENTE.name())
                .message("La tarea ha sido encolada correctamente. Consulta usando el jobId.")
                .build();

        return ResponseEntity.accepted().body(response);
    }

    @GetMapping("/result/{jobId}")
    public ResponseEntity<PredictionResponse> getResult(@PathVariable String jobId) {
        String statusStr = resultService.getStatus(jobId);

        if (statusStr == null) {
            return ResponseEntity.notFound().build();
        }

        JobStatus status = JobStatus.valueOf(statusStr);
        PredictionResponse.PredictionResponseBuilder responseBuilder = PredictionResponse.builder()
                .jobId(jobId)
                .status(status.name());

        if (status == JobStatus.COMPLETADO || status == JobStatus.ERROR) {
            responseBuilder.result(resultService.getResult(jobId));
        }

        return ResponseEntity.ok(responseBuilder.build());
    }
}
