package com.deepLearning.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Servicio encargado de gestionar el estado de las tareas en Redis.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DlResultService {

    private final StringRedisTemplate redisTemplate;

    // Expiración para no llenar la RAM de Redis eternamente (ej. 24 horas)
    private static final Duration KEY_EXPIRATION = Duration.ofHours(24);

    public void saveInitialState(String jobId) {
        log.info("Guardando estado inicial en Redis para Job: {}", jobId);
        redisTemplate.opsForValue().set(jobId + ":status", "PENDIENTE", KEY_EXPIRATION);
    }

    public void saveProcessingState(String jobId) {
        redisTemplate.opsForValue().set(jobId + ":status", "PROCESANDO", KEY_EXPIRATION);
    }

    public void saveFinalResult(String jobId, String result) {
        log.info("Guardando resultado final en Redis para Job: {}", jobId);
        redisTemplate.opsForValue().set(jobId + ":status", "COMPLETADO", KEY_EXPIRATION);
        redisTemplate.opsForValue().set(jobId + ":result", result, KEY_EXPIRATION);
    }

    public void saveErrorState(String jobId, String errorMessage) {
        log.error("Guardando estado de ERROR en Redis para Job: {}", jobId);
        redisTemplate.opsForValue().set(jobId + ":status", "ERROR", KEY_EXPIRATION);
        redisTemplate.opsForValue().set(jobId + ":result", errorMessage, KEY_EXPIRATION);
    }

    public String getStatus(String jobId) {
        return redisTemplate.opsForValue().get(jobId + ":status");
    }

    public String getResult(String jobId) {
        return redisTemplate.opsForValue().get(jobId + ":result");
    }
}