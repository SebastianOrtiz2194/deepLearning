package com.deepLearning.dto;

import lombok.Data;
import lombok.Builder;

/**
 * DTO para devolver el estado y/o el resultado de la inferencia al cliente.
 */
@Data
@Builder
public class PredictionResponse {

    private String jobId;
    private String status; // EJ: PENDIENTE, PROCESANDO, COMPLETADO, ERROR
    private String message;

    // Aquí irá el resultado en formato JSON (String) o un objeto estructurado
    private String result;
}
