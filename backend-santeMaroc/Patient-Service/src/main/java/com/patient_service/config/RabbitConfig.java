package com.patient_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration RabbitMQ pour le Patient
 */
@Configuration
public class RabbitConfig {

    // ⚡ Communication avec Provider
    public static final String PATIENT_EXCHANGE = "patient-exchange";
    public static final String PATIENT_STATUS_QUEUE = "patient.status.queue";
    public static final String PATIENT_STATUS_ROUTING_KEY = "patient.status.update";
    public static final String PATIENT_SYNC_QUEUE = "patient.sync.queue";
    public static final String PATIENT_SYNC_ROUTING_KEY = "patient.sync.request";
    public static final String PATIENT_SYNC_REQUEST_QUEUE = "patient.sync.request.queue";
    public static final String PATIENT_SYNC_REQUEST_ROUTING_KEY = "patient.sync.request.provider";
    public static final String PATIENT_SYNC_RESPONSE_QUEUE = "patient.sync.response.queue";
    public static final String PATIENT_SYNC_RESPONSE_ROUTING_KEY = "patient.sync.response";
    
    // ⚡ Communication pour les réponses aux demandes depuis Request-Service
    public static final String REQUEST_RESPONSES_EXCHANGE = "request.responses.exchange";
    public static final String REQUEST_RESPONSES_QUEUE = "request.responses.queue";
    public static final String REQUEST_RESPONSES_ROUTING_KEY = "request.responses.key";
    
    // ⚡ Communication pour les demandes patients
    public static final String PATIENT_REQUESTS_EXCHANGE = "patient.requests.exchange";
    public static final String PATIENT_REQUESTS_QUEUE = "patient.requests.queue";
    public static final String PATIENT_REQUESTS_ROUTING_KEY = "patient.requests.key";

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        return factory;
    }

    // ⚡ Exchange principal
    @Bean
    public TopicExchange patientExchange() {
        return new TopicExchange(PATIENT_EXCHANGE, true, false);
    }

    // ⚡ Queues
    @Bean
    public Queue patientStatusQueue() {
        return QueueBuilder.durable(PATIENT_STATUS_QUEUE).build();
    }

    @Bean
    public Queue patientSyncQueue() {
        return QueueBuilder.durable(PATIENT_SYNC_QUEUE).build();
    }

    @Bean
    public Queue patientSyncRequestQueue() {
        return QueueBuilder.durable(PATIENT_SYNC_REQUEST_QUEUE).build();
    }

    @Bean
    public Queue patientSyncResponseQueue() {
        return QueueBuilder.durable(PATIENT_SYNC_RESPONSE_QUEUE).build();
    }

    // ⚡ Bindings
    @Bean
    public Binding patientStatusBinding() {
        return BindingBuilder
                .bind(patientStatusQueue())
                .to(patientExchange())
                .with(PATIENT_STATUS_ROUTING_KEY);
    }

    @Bean
    public Binding patientSyncBinding() {
        return BindingBuilder
                .bind(patientSyncQueue())
                .to(patientExchange())
                .with(PATIENT_SYNC_ROUTING_KEY);
    }

    @Bean
    public Binding patientSyncRequestBinding() {
        return BindingBuilder
                .bind(patientSyncRequestQueue())
                .to(patientExchange())
                .with(PATIENT_SYNC_REQUEST_ROUTING_KEY);
    }

    @Bean
    public Binding patientSyncResponseBinding() {
        return BindingBuilder
                .bind(patientSyncResponseQueue())
                .to(patientExchange())
                .with(PATIENT_SYNC_RESPONSE_ROUTING_KEY);
    }

    // ⚡ Exchange pour les demandes patients
    @Bean
    public TopicExchange patientRequestsExchange() {
        return new TopicExchange(PATIENT_REQUESTS_EXCHANGE, true, false);
    }

    // ⚡ Queue pour les demandes patients
    @Bean
    public Queue patientRequestsQueue() {
        return QueueBuilder.durable(PATIENT_REQUESTS_QUEUE).build();
    }

    // ⚡ Binding pour les demandes patients
    @Bean
    public Binding patientRequestsBinding() {
        return BindingBuilder
                .bind(patientRequestsQueue())
                .to(patientRequestsExchange())
                .with(PATIENT_REQUESTS_ROUTING_KEY);
    }

    // ⚡ Exchange pour recevoir les réponses depuis Request-Service
    @Bean
    public TopicExchange requestResponsesExchange() {
        return new TopicExchange(REQUEST_RESPONSES_EXCHANGE, true, false);
    }

    // ⚡ Queue pour recevoir les réponses depuis Request-Service
    @Bean
    public Queue requestResponsesQueue() {
        return QueueBuilder.durable(REQUEST_RESPONSES_QUEUE).build();
    }

    // ⚡ Binding pour recevoir les réponses depuis Request-Service
    @Bean
    public Binding requestResponsesBinding() {
        return BindingBuilder
                .bind(requestResponsesQueue())
                .to(requestResponsesExchange())
                .with(REQUEST_RESPONSES_ROUTING_KEY);
    }
}
