package com.provider_service.services;

import com.provider_service.config.RabbitConfig;
import com.provider_service.dto.CreateMedicalRecordRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * Service pour publier les demandes de création de dossiers médicaux via RabbitMQ.
 * 
 * Ce service permet au Provider-Service d'envoyer des demandes de création
 * de dossiers médicaux au Medicalrecord-Service via RabbitMQ.
 * 
 * @author Provider-Service Team
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MedicalRecordPublisherService {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Publie une demande de création de dossier médical au Medicalrecord-Service via RabbitMQ.
     * 
     * @param request La demande de création de dossier médical
     */
    public void publishCreateMedicalRecord(CreateMedicalRecordRequest request) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitConfig.MEDICAL_RECORD_EXCHANGE,
                    RabbitConfig.MEDICAL_RECORD_CREATE_ROUTING_KEY,
                    request
            );
            
            log.info("✅ Demande de création de dossier médical publiée pour le patient {} par le provider {}", 
                    request.getPatientId(), request.getProviderId());
        } catch (Exception e) {
            log.error("❌ Erreur lors de la publication de la demande de création de dossier médical : {}", 
                    e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la publication de la demande de création de dossier médical", e);
        }
    }
}

