package com.request_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration RabbitMQ pour le Request-Service.
 * 
 * Ce service communique avec :
 * - Patient-Service : reçoit les demandes de patients
 * - Patient-Service : envoie les réponses aux patients
 * 
 * @author Request-Service Team
 * @version 1.0
 */
@Configuration
public class RabbitConfig {

    // ==================== CONSTANTES ====================
    
    /** Exchange pour recevoir les demandes depuis Patient-Service */
    public static final String PATIENT_REQUESTS_EXCHANGE = "patient.requests.exchange";
    
    /** Queue pour recevoir les demandes depuis Patient-Service */
    public static final String PATIENT_REQUESTS_QUEUE = "patient.requests.queue";
    
    /** Routing key pour les demandes depuis Patient-Service */
    public static final String PATIENT_REQUESTS_ROUTING_KEY = "patient.requests.key";
    
    /** Exchange pour envoyer les réponses vers Patient-Service */
    public static final String REQUEST_RESPONSES_EXCHANGE = "request.responses.exchange";
    
    /** Routing key pour les réponses vers Patient-Service */
    public static final String REQUEST_RESPONSES_ROUTING_KEY = "request.responses.key";

    // ==================== MESSAGE CONVERTER ====================
    
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // ==================== RABBIT TEMPLATE ====================
    
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }

    // ==================== LISTENER CONTAINER FACTORY ====================
    
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        return factory;
    }

    // ==================== EXCHANGES ====================
    
    /**
     * Exchange pour recevoir les demandes depuis Patient-Service.
     */
    @Bean
    public TopicExchange patientRequestsExchange() {
        return new TopicExchange(PATIENT_REQUESTS_EXCHANGE, true, false);
    }

    /**
     * Exchange pour envoyer les réponses vers Patient-Service.
     */
    @Bean
    public TopicExchange requestResponsesExchange() {
        return new TopicExchange(REQUEST_RESPONSES_EXCHANGE, true, false);
    }

    // ==================== QUEUES ====================
    
    /**
     * Queue pour recevoir les demandes depuis Patient-Service.
     */
    @Bean
    public Queue patientRequestsQueue() {
        return QueueBuilder.durable(PATIENT_REQUESTS_QUEUE).build();
    }

    // ==================== BINDINGS ====================
    
    /**
     * Binding pour recevoir les demandes depuis Patient-Service.
     */
    @Bean
    public Binding patientRequestsBinding() {
        return BindingBuilder
                .bind(patientRequestsQueue())
                .to(patientRequestsExchange())
                .with(PATIENT_REQUESTS_ROUTING_KEY);
    }
}

