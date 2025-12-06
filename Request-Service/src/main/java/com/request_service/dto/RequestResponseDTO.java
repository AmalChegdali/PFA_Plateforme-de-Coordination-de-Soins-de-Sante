package com.request_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour les réponses aux demandes.
 * 
 * @author Request-Service Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestResponseDTO {
    
    private String requestId;
    private String patientId; // ✅ Ajouté pour identifier le patient à notifier
    private String status;
    private String message;
    private String providerId; // Provider qui a répondu
    private String providerName; // Nom du provider qui a répondu
}

