package com.patient_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    // Exchange and Queue Names
    public static final String PATIENT_EXCHANGE = "patient.exchange";
    public static final String PATIENT_STATUS_QUEUE = "patient.status.queue";
    public static final String PATIENT_STATUS_ROUTING_KEY = "patient.status.update";
    public static final String PATIENT_SYNC_QUEUE = "patient.sync.queue";
    public static final String PATIENT_SYNC_ROUTING_KEY = "patient.sync.request";
    public static final String PATIENT_SYNC_RESPONSE_QUEUE = "patient.sync.response.queue";
    public static final String PATIENT_SYNC_RESPONSE_ROUTING_KEY = "patient.sync.response";

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

    // Exchange
    @Bean
    public TopicExchange patientExchange() {
        return new TopicExchange(PATIENT_EXCHANGE, true, false);
    }

    // Queues
    @Bean
    public Queue patientStatusQueue() {
        return QueueBuilder.durable(PATIENT_STATUS_QUEUE).build();
    }

    @Bean
    public Queue patientSyncQueue() {
        return QueueBuilder.durable(PATIENT_SYNC_QUEUE).build();
    }
    
    @Bean
    public Queue patientSyncResponseQueue() {
        return QueueBuilder.durable(PATIENT_SYNC_RESPONSE_QUEUE).build();
    }

    // Bindings
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
    public Binding patientSyncResponseBinding() {
        return BindingBuilder
                .bind(patientSyncResponseQueue())
                .to(patientExchange())
                .with(PATIENT_SYNC_RESPONSE_ROUTING_KEY);
    }
}
