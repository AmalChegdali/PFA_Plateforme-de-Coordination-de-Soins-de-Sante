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

        // ✅ Publication dans la queue RabbitMQ
        rabbitTemplate.convertAndSend(
                "patient.requests.exchange",
                "patient.requests.key",
                message
        );

        return new PatientRequestResponseDTO(
                requestId,
                "QUEUED",
                "Votre demande a été transmise au secrétariat."
        );
    }

    // ✅ 2. Historique (Mock temporaire)
    public List<PatientRequestResponseDTO> getPatientRequests(Authentication authentication) {
        return List.of(
                new PatientRequestResponseDTO("req-1", "TRAITÉ", "Demande validée"),
                new PatientRequestResponseDTO("req-2", "EN_ATTENTE", "En cours de traitement")
        );
    }

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
