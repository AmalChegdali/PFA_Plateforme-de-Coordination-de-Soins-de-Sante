package com.provider_service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * DTO pour envoyer les mises à jour de statut de patient à Patient-Service via RabbitMQ.
 * Format compatible avec Patient-Service.
 */
public class PatientStatusUpdateMessageDTO {
    private String patientId;
    private String providerId;
    private String newStatus;
    private String previousStatus;
    private String reason;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    // Constructeur par défaut
    public PatientStatusUpdateMessageDTO() {
        this.timestamp = LocalDateTime.now();
    }

    // Constructeur avec paramètres principaux
    public PatientStatusUpdateMessageDTO(String patientId, String providerId, 
                                        String newStatus, String previousStatus) {
        this.patientId = patientId;
        this.providerId = providerId;
        this.newStatus = newStatus;
        this.previousStatus = previousStatus;
        this.timestamp = LocalDateTime.now();
    }

    // Constructeur complet
    public PatientStatusUpdateMessageDTO(String patientId, String providerId, 
                                        String newStatus, String previousStatus, String reason) {
        this.patientId = patientId;
        this.providerId = providerId;
        this.newStatus = newStatus;
        this.previousStatus = previousStatus;
        this.reason = reason;
        this.timestamp = LocalDateTime.now();
    }

    // Getters et Setters
    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public String getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(String previousStatus) {
        this.previousStatus = previousStatus;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}

