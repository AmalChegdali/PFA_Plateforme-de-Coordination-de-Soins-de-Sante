package com.medicalrecord_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration RabbitMQ pour le Medicalrecord-Service.
 * 
 * Cette classe configure :
 * - Les exchanges, queues et bindings pour la communication avec Provider-Service
 * - Le message converter JSON pour la sérialisation/désérialisation des messages
 * - Le RabbitTemplate et les listeners pour recevoir les messages
 * 
 * @author MedicalRecord-Service Team
 * @version 1.0
 */
@Configuration
public class RabbitConfig {

    // ==================== CONSTANTES ====================
    
    /** Nom de l'exchange principal pour la communication avec Provider-Service */
    public static final String MEDICAL_RECORD_EXCHANGE = "medical-record-exchange";
    
    /** Queue pour recevoir les demandes de création de dossiers médicaux depuis Provider-Service */
    public static final String MEDICAL_RECORD_CREATE_QUEUE = "medical.record.create.queue";
    
    /** Routing key pour la création de dossiers médicaux */
    public static final String MEDICAL_RECORD_CREATE_ROUTING_KEY = "medical.record.create";

    // ==================== MESSAGE CONVERTER ====================
    
    /**
     * Configure le convertisseur de messages JSON.
     * Utilisé pour sérialiser/désérialiser les objets Java en JSON pour RabbitMQ.
     * 
     * @return Jackson2JsonMessageConverter configuré
     */
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // ==================== RABBIT TEMPLATE ====================
    
    /**
     * Configure le RabbitTemplate pour envoyer des messages.
     * Le template utilise le message converter JSON pour la sérialisation.
     * 
     * @param connectionFactory La factory de connexion RabbitMQ
     * @return RabbitTemplate configuré avec le message converter
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }

    // ==================== LISTENER CONTAINER FACTORY ====================
    
    /**
     * Configure la factory pour les listeners RabbitMQ.
     * Utilisée par @RabbitListener pour désérialiser les messages reçus.
     * 
     * @param connectionFactory La factory de connexion RabbitMQ
     * @return SimpleRabbitListenerContainerFactory configuré
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        return factory;
    }

    // ==================== EXCHANGE ====================
    
    /**
     * Crée l'exchange Topic principal pour la communication avec Provider-Service.
     * Un TopicExchange permet d'utiliser des routing keys avec patterns.
     * 
     * @return TopicExchange durable et non-auto-delete
     */
    @Bean
    public TopicExchange medicalRecordExchange() {
        return new TopicExchange(MEDICAL_RECORD_EXCHANGE, true, false);
    }

    // ==================== QUEUES ====================
    
    /**
     * Crée la queue durable pour recevoir les demandes de création de dossiers médicaux.
     * Cette queue reçoit les messages lorsque des providers créent des dossiers médicaux.
     * 
     * @return Queue durable nommée MEDICAL_RECORD_CREATE_QUEUE
     */
    @Bean
    public Queue medicalRecordCreateQueue() {
        return QueueBuilder.durable(MEDICAL_RECORD_CREATE_QUEUE).build();
    }

    // ==================== BINDINGS ====================
    
    /**
     * Lie la queue de création à l'exchange avec le routing key spécifique.
     * Les messages avec le routing key "medical.record.create" seront routés vers cette queue.
     * 
     * @return Binding entre medicalRecordCreateQueue et medicalRecordExchange
     */
    @Bean
    public Binding medicalRecordCreateBinding() {
        return BindingBuilder
                .bind(medicalRecordCreateQueue())
                .to(medicalRecordExchange())
                .with(MEDICAL_RECORD_CREATE_ROUTING_KEY);
    }
}

