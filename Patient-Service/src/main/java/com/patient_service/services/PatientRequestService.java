package com.patient_service.services;

import com.patient_service.dto.*;
import com.patient_service.enums.AccountStatus;
import com.patient_service.models.Patient;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PatientRequestService {

    private final RabbitTemplate rabbitTemplate;

    public PatientRequestService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // ✅ 1. Envoi vers RabbitMQ
    public PatientRequestResponseDTO submitRequest(
            PatientRequestDTO dto,
            Authentication authentication
    ) {
        Patient patient = (Patient) authentication.getPrincipal();
        
        // Vérification supplémentaire du statut du compte (double sécurité)
        if (patient.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException(
                "Le compte n'est pas activé. Statut actuel: " + patient.getAccountStatus() + 
                ". Veuillez attendre l'approbation du prestataire de santé."
            );
        }

        String requestId = UUID.randomUUID().toString();

        Map<String, Object> message = new HashMap<>();
        message.put("requestId", requestId);
        message.put("patientId", patient.getId());
        message.put("type", dto.getType());
        message.put("priority", dto.getPriority());
        message.put("subject", dto.getSubject());
        message.put("description", dto.getDescription());
        message.put("preferredDate", dto.getPreferredDate());
        // Ajouter le provider cible si spécifié
        if (dto.getTargetProviderId() != null && !dto.getTargetProviderId().isEmpty()) {
            message.put("targetProviderId", dto.getTargetProviderId());
        }

        // ✅ Publication dans la queue RabbitMQ
        rabbitTemplate.convertAndSend(
                com.patient_service.config.RabbitConfig.PATIENT_REQUESTS_EXCHANGE,
                com.patient_service.config.RabbitConfig.PATIENT_REQUESTS_ROUTING_KEY,
                message
        );

        return new PatientRequestResponseDTO(
                requestId,
                "QUEUED",
                "Votre demande a été transmise au secrétariat."
        );
    }

    // Note: La récupération des demandes se fait maintenant uniquement dans Request-Service
    // Utiliser GET /api/requests/patient/{patientId} dans Request-Service

    // ✅ 3. Ajout message
    public void addMessage(
            String requestId,
            RequestMessageDTO dto,
            Authentication authentication
    ) {
        Map<String, Object> message = new HashMap<>();
        message.put("requestId", requestId);
        message.put("content", dto.getContent());

        rabbitTemplate.convertAndSend(
                "patient.messages.exchange",
                "patient.messages.key",
                message
        );
    }
}
