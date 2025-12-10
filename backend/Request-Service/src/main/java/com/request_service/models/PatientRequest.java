package com.request_service.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Modèle pour représenter une demande de patient.
 * 
 * @author Request-Service Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "patient_requests")
public class PatientRequest {

    @Id
    private String id;
    
    private String requestId;
    private String patientId;
    private String patientEmail;
    private String patientName;
    
    private String type; // Appointment, Consultation, Prescription, etc.
    private String priority; // High, Medium, Low
    private String subject;
    private String description;
    private String preferredDate;
    
    private String status; // EN_ATTENTE, TRAITÉ, REFUSÉ, EN_COURS
    
    private String targetProviderId; // Provider cible (spécifié par le patient)
    private String providerId; // Provider qui a traité la demande
    private String providerName;
    private String responseMessage;
    private LocalDateTime responseDate;
    
    private List<RequestMessage> messages = new ArrayList<>();
    private Map<String, Object> metadata = new HashMap<>();
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Classe interne pour représenter un message dans une demande.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequestMessage {
        private String senderId; // patientId ou providerId
        private String senderType; // PATIENT ou PROVIDER
        private String content;
        private LocalDateTime timestamp;
    }
}

