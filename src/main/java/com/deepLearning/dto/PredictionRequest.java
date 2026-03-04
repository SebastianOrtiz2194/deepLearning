package com.deepLearning.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO (Data Transfer Object) para recibir la petición del cliente.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PredictionRequest {

    // URL de la imagen, ruta de archivo, o texto base64
    private String dataPayload;

    // Opcional: Para indicar qué modelo usar si tenemos varios
    private String modelType;
}
