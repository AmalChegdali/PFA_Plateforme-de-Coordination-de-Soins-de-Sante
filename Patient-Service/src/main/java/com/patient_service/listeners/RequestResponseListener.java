package com.patient_service.listeners;

import com.patient_service.config.RabbitConfig;
import com.patient_service.dto.PatientRequestResponseDTO;
import com.patient_service.services.NotificationService;
import com.patient_service.services.RequestResponseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Listener RabbitMQ pour recevoir les réponses aux demandes depuis Request-Service.
 * 
 * @author Patient-Service Team
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RequestResponseListener {

    private final RequestResponseService requestResponseService;
    private final NotificationService notificationService;

    /**
     * Écoute les réponses aux demandes depuis Request-Service.
     * Reçoit un Map depuis RabbitMQ (car RequestResponseDTO est dans Request-Service).
     * 
     * @param responseMap Le Map contenant la réponse reçue
     */
    @RabbitListener(queues = RabbitConfig.REQUEST_RESPONSES_QUEUE)
    public void handleRequestResponse(Map<String, Object> responseMap) {
        log.info("📨 Réception d'une réponse à une demande depuis Request-Service");
        
        try {
            // ✅ 1. Convertir le Map en PatientRequestResponseDTO
            PatientRequestResponseDTO response = convertToPatientRequestResponseDTO(responseMap);
            
            if (response == null || response.getRequestId() == null) {
                log.warn("⚠️ Réponse invalide reçue");
                return;
            }
            
            log.info("   Request ID: {}", response.getRequestId());
            log.info("   Patient ID: {}", response.getRequestId()); // patientId sera dans le DTO
            log.info("   Status: {}", response.getStatus());
            log.info("   Message: {}", response.getMessage());
            
            // ✅ 2. Enregistrer la réponse dans le cache
            requestResponseService.saveResponse(response);
            
            // ✅ 3. Extraire le patientId depuis la réponse
            String patientId = extractPatientIdFromResponse(response);
            
            if (patientId != null) {
                // ✅ 4. Notifier le patient
                notificationService.notifyPatient(response, patientId);
                log.info("✅ Réponse traitée et patient notifié pour la demande: {} - Patient: {}", 
                        response.getRequestId(), patientId);
            } else {
                log.warn("⚠️ Impossible de notifier le patient : patientId non trouvé dans la réponse");
            }
            
        } catch (Exception e) {
            log.error("❌ Erreur lors du traitement de la réponse : {}", e.getMessage(), e);
        }
    }

    /**
     * Convertit un Map (reçu depuis RabbitMQ) en PatientRequestResponseDTO.
     * 
     * @param responseMap Le Map contenant les données de la réponse
     * @return PatientRequestResponseDTO ou null si conversion impossible
     */
    private PatientRequestResponseDTO convertToPatientRequestResponseDTO(Map<String, Object> responseMap) {
        try {
            PatientRequestResponseDTO dto = new PatientRequestResponseDTO();
            
            if (responseMap.get("requestId") != null) {
                dto.setRequestId(responseMap.get("requestId").toString());
            }
            if (responseMap.get("patientId") != null) {
                dto.setPatientId(responseMap.get("patientId").toString());
            }
            if (responseMap.get("status") != null) {
                dto.setStatus(responseMap.get("status").toString());
            }
            if (responseMap.get("message") != null) {
                dto.setMessage(responseMap.get("message").toString());
                dto.setResponseMessage(responseMap.get("message").toString());
            }
            if (responseMap.get("providerId") != null) {
                dto.setProviderId(responseMap.get("providerId").toString());
            }
            if (responseMap.get("providerName") != null) {
                dto.setProviderName(responseMap.get("providerName").toString());
            }
            
            dto.setUpdatedAt(LocalDateTime.now());
            
            return dto;
        } catch (Exception e) {
            log.error("❌ Erreur lors de la conversion de la réponse : {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Extrait le patientId depuis la réponse.
     * 
     * @param response La réponse
     * @return Le patientId ou null si non trouvé
     */
    private String extractPatientIdFromResponse(PatientRequestResponseDTO response) {
        return response.getPatientId();
    }
}

