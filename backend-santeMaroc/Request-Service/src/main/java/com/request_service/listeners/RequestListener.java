package com.request_service.listeners;

import com.request_service.config.RabbitConfig;
import com.request_service.dto.PatientRequestMessageDTO;
import com.request_service.services.PatientRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Listener RabbitMQ pour recevoir les demandes depuis Patient-Service.
 * 
 * @author Request-Service Team
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RequestListener {

    private final PatientRequestService patientRequestService;

    /**
     * Écoute les demandes de patients depuis Patient-Service.
     * 
     * @param message Le message Map contenant la demande
     */
    @RabbitListener(queues = RabbitConfig.PATIENT_REQUESTS_QUEUE)
    public void handlePatientRequest(Map<String, Object> message) {
        log.info("📨 Réception d'une demande de patient via RabbitMQ");
        
        try {
            // Convertir le Map en PatientRequestMessageDTO
            PatientRequestMessageDTO requestDTO = convertToRequestDTO(message);
            
            // Créer la demande dans MongoDB
            patientRequestService.createRequest(requestDTO);
            
            log.info("✅ Demande traitée : {} - Patient: {} - Sujet: {}", 
                    requestDTO.getRequestId(), requestDTO.getPatientId(), requestDTO.getSubject());
        } catch (Exception e) {
            log.error("❌ Erreur lors du traitement de la demande : {}", e.getMessage(), e);
        }
    }

    /**
     * Convertit un Map (message RabbitMQ) en PatientRequestMessageDTO.
     */
    @SuppressWarnings("unchecked")
    private PatientRequestMessageDTO convertToRequestDTO(Map<String, Object> message) {
        PatientRequestMessageDTO dto = new PatientRequestMessageDTO();
        
        if (message.get("requestId") != null) {
            dto.setRequestId(message.get("requestId").toString());
        }
        if (message.get("patientId") != null) {
            dto.setPatientId(message.get("patientId").toString());
        }
        if (message.get("patientEmail") != null) {
            dto.setPatientEmail(message.get("patientEmail").toString());
        }
        if (message.get("patientName") != null) {
            dto.setPatientName(message.get("patientName").toString());
        }
        if (message.get("type") != null) {
            dto.setType(message.get("type").toString());
        }
        if (message.get("priority") != null) {
            dto.setPriority(message.get("priority").toString());
        }
        if (message.get("subject") != null) {
            dto.setSubject(message.get("subject").toString());
        }
        if (message.get("description") != null) {
            dto.setDescription(message.get("description").toString());
        }
        if (message.get("preferredDate") != null) {
            dto.setPreferredDate(message.get("preferredDate").toString());
        }
        if (message.get("status") != null) {
            dto.setStatus(message.get("status").toString());
        }
        if (message.get("targetProviderId") != null) {
            dto.setTargetProviderId(message.get("targetProviderId").toString());
            log.info("📌 Demande destinée au provider : {}", dto.getTargetProviderId());
        }
        if (message.get("metadata") != null && message.get("metadata") instanceof Map) {
            dto.setMetadata((Map<String, Object>) message.get("metadata"));
        }
        
        return dto;
    }
}

