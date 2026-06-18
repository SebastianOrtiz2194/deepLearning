package com.deeplearning.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Inference Worker.
 *
 * <p>Consumes inference tasks from Kafka, executes image classification
 * using a pre-trained DJL model, and persists results to Redis.
 * Designed to scale independently from the API service.
 *
 * @since 1.0.0
 */
@SpringBootApplication
public class WorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkerApplication.class, args);
    }
}
