package com.request_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO pour représenter une demande de patient (utilisé pour RabbitMQ et API).
 * 
 * @author Request-Service Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientRequestMessageDTO {
    
    private String requestId;
    private String patientId;
    private String patientEmail;
    private String patientName;
    
    private String type;
    private String priority;
    private String subject;
    private String description;
    private String preferredDate;
    
    private String status;
    
    private String targetProviderId; // Provider cible (spécifié par le patient)
    private String providerId; // Provider qui a traité la demande
    private String providerName;
    private String responseMessage;
    private LocalDateTime responseDate;
    
    private List<MessageDTO> messages;
    private Map<String, Object> metadata;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * DTO pour représenter un message dans une demande.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageDTO {
        private String senderId;
        private String senderType;
        private String content;
        private LocalDateTime timestamp;
    }
}

