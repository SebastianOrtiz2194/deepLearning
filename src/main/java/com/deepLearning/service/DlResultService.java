package com.deepLearning.service;

import com.deepLearning.enums.JobStatus;
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
        saveStatus(jobId, JobStatus.PENDIENTE);
    }

    public void saveProcessingState(String jobId) {
        saveStatus(jobId, JobStatus.PROCESANDO);
    }

    public void saveFinalResult(String jobId, String result) {
        log.info("Guardando resultado final en Redis para Job: {}", jobId);
        saveStatus(jobId, JobStatus.COMPLETADO);
        redisTemplate.opsForValue().set(jobId + ":result", result, KEY_EXPIRATION);
    }

    public void saveErrorState(String jobId, String errorMessage) {
        log.error("Guardando estado de ERROR en Redis para Job: {}", jobId);
        saveStatus(jobId, JobStatus.ERROR);
        redisTemplate.opsForValue().set(jobId + ":result", errorMessage, KEY_EXPIRATION);
    }

    private void saveStatus(String jobId, JobStatus status) {
        redisTemplate.opsForValue().set(jobId + ":status", status.name(), KEY_EXPIRATION);
    }

    public String getStatus(String jobId) {
        return redisTemplate.opsForValue().get(jobId + ":status");
    }

    public String getResult(String jobId) {
        return redisTemplate.opsForValue().get(jobId + ":result");
    }
}
