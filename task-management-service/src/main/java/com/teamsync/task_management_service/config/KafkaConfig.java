package com.teamsync.task_management_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {

    @Bean
    public DefaultErrorHandler defaultErrorHandler() {
        return new DefaultErrorHandler(
            (consumerRecord, exception) -> {
                // Log the error and continue processing other messages
                System.err.println("Error processing Kafka message: " + exception.getMessage());
            },
            new FixedBackOff(1000L, 3) // Retry 3 times with 1 second delay
        );
    }
}
