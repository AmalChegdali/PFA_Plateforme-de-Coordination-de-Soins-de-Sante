package com.request_service.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Modèle pour représenter un certificat médical.
 * 
 * @author Request-Service Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "certificates")
public class Certificate {

    @Id
    private String id;
    
    private String certificateId; // ID unique du certificat (ex: CERT-2025-001)
    private String requestId; // ID de la demande associée
    private String patientId;
    private String patientName;
    private String patientEmail;
    private String providerId;
    private String providerName;
    
    private String type; // Type de certificat (MEDICAL, WORK, SPORT, etc.)
    private String title; // Titre du certificat
    private String content; // Contenu détaillé du certificat
    private LocalDate issueDate; // Date d'émission
    private LocalDate expiryDate; // Date d'expiration (optionnel)
    
    private String status; // ACTIVE, EXPIRED, REVOKED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


