package com.deeplearning.worker.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Kafka consumer configuration for the inference worker.
 *
 * <p>Sets up the listener container factory with a custom error handler
 * that logs failures and provides a fixed backoff before retries.
 *
 * @since 1.0.0
 */
@Configuration
public class KafkaConsumerConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerConfig.class);

    /**
     * Provides a custom error handler that logs Kafka listener failures.
     *
     * <p>Uses a fixed backoff of 10 seconds with 3 retry attempts.
     * After exhausting retries, the message is logged and discarded.
     *
     * @return a configured {@link CommonErrorHandler}
     */
    @Bean
    public CommonErrorHandler commonErrorHandler() {
        DefaultErrorHandler handler = new DefaultErrorHandler((record, exception) -> {
            log.error("kafka_listener_exhausted topic={} partition={} offset={}",
                    record.topic(), record.partition(), record.offset(), exception);
            // TODO: Dead-letter queue — publish failed messages to a DLQ topic
        }, new FixedBackOff(10_000L, 3L));

        handler.addNotRetryableExceptions(org.apache.kafka.common.errors.SerializationException.class);
        return handler;
    }

    /**
     * Configures the Kafka listener container factory with the custom error handler.
     *
     * @param consumerFactory the auto-configured consumer factory
     * @return a customized concurrent container factory
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory, CommonErrorHandler errorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }
}
