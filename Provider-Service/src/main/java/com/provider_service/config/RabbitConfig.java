package com.provider_service.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String PATIENT_EXCHANGE = "patient-exchange";
    public static final String PATIENT_SYNC_ROUTING_KEY = "patient.sync.request";
    public static final String PATIENT_STATUS_ROUTING_KEY = "patient.status.update";
    public static final String PATIENT_SYNC_RESPONSE_QUEUE = "patient.sync.response.queue";

    @Bean
    public TopicExchange patientExchange() {
        return new TopicExchange(PATIENT_EXCHANGE);
    }

    @Bean
    public Queue patientSyncResponseQueue() {
        return new Queue(PATIENT_SYNC_RESPONSE_QUEUE, false);
    }
}
