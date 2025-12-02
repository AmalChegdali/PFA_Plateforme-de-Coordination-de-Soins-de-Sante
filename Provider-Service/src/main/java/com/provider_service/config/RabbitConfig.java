package com.provider_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration RabbitMQ pour le Provider-Service.
 * 
 * Cette classe configure :
 * - Les exchanges, queues et bindings pour la communication avec Patient-Service
 * - Le message converter JSON pour la sérialisation/désérialisation des messages
 * - Le RabbitTemplate et les listeners pour recevoir les messages
 * 
 * @author Provider-Service Team
 * @version 1.0
 */
@Configuration
public class RabbitConfig {

    // ==================== CONSTANTES ====================
    
    /** Nom de l'exchange principal pour la communication avec Patient-Service */
    public static final String PATIENT_EXCHANGE = "patient-exchange";
    
    /** Queue pour recevoir les nouveaux patients depuis Patient-Service */
    public static final String PATIENT_SYNC_QUEUE = "patient.sync.queue";
    
    /** Routing key pour la synchronisation des patients */
    public static final String PATIENT_SYNC_ROUTING_KEY = "patient.sync.request";
    
    /** Queue pour les demandes de patients (réservée pour futures fonctionnalités) */
    public static final String PATIENT_REQUESTS_QUEUE = "patient.requests.queue";
    
    /** Routing key pour les mises à jour de statut des patients */
    public static final String PATIENT_STATUS_ROUTING_KEY = "patient.status.update";
    
    /** Routing key pattern pour les demandes de patients */
    private static final String PATIENT_REQUESTS_ROUTING_PATTERN = "patient.requests.*";

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
     * Crée l'exchange Topic principal pour la communication avec Patient-Service.
     * Un TopicExchange permet d'utiliser des routing keys avec patterns.
     * 
     * @return TopicExchange durable et non-auto-delete
     */
    @Bean
    public TopicExchange patientExchange() {
        return new TopicExchange(PATIENT_EXCHANGE, true, false);
    }

    // ==================== QUEUES ====================
    
    /**
     * Crée la queue durable pour recevoir les nouveaux patients.
     * Cette queue reçoit les messages lorsque des patients s'inscrivent dans Patient-Service.
     * 
     * @return Queue durable nommée PATIENT_SYNC_QUEUE
     */
    @Bean
    public Queue patientSyncQueue() {
        return QueueBuilder.durable(PATIENT_SYNC_QUEUE).build();
    }

    /**
     * Crée la queue durable pour les demandes de patients.
     * Réservée pour de futures fonctionnalités (ex: demandes de rendez-vous).
     * 
     * @return Queue durable nommée PATIENT_REQUESTS_QUEUE
     */
    @Bean
    public Queue patientRequestsQueue() {
        return QueueBuilder.durable(PATIENT_REQUESTS_QUEUE).build();
    }

    // ==================== BINDINGS ====================
    
    /**
     * Lie la queue de synchronisation à l'exchange avec le routing key spécifique.
     * Les messages avec le routing key "patient.sync.request" seront routés vers cette queue.
     * 
     * @return Binding entre patientSyncQueue et patientExchange
     */
    @Bean
    public Binding patientSyncBinding() {
        return BindingBuilder
                .bind(patientSyncQueue())
                .to(patientExchange())
                .with(PATIENT_SYNC_ROUTING_KEY);
    }

    /**
     * Lie la queue des demandes à l'exchange avec un pattern de routing key.
     * Les messages avec un routing key commençant par "patient.requests." seront routés vers cette queue.
     * 
     * @return Binding entre patientRequestsQueue et patientExchange
     */
    @Bean
    public Binding patientRequestsBinding() {
        return BindingBuilder
                .bind(patientRequestsQueue())
                .to(patientExchange())
                .with(PATIENT_REQUESTS_ROUTING_PATTERN);
    }
}
