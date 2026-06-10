package com.deeplearning.inference;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Inference Service.
 *
 * <p>Exposes a REST API that accepts image classification requests,
 * publishes them to Kafka for asynchronous processing, and exposes
 * endpoints to query job status and retrieve results from Redis.
 *
 * @since 1.0.0
 */
@SpringBootApplication
public class InferenceServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InferenceServiceApplication.class, args);
    }
}
