package com.medicalrecord_service.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO pour les demandes de création de dossiers médicaux via RabbitMQ.
 * 
 * Ce DTO est utilisé pour recevoir les demandes de création de dossiers médicaux
 * depuis le Provider-Service via RabbitMQ.
 * 
 * @author MedicalRecord-Service Team
 * @version 1.0
 */
public class CreateMedicalRecordRequest {
    
    private String recordId;
    private String patientId;
    private String providerId;
    private String recordType;
    private LocalDateTime visitDate;
    private String diagnosis;
    private Map<String, Object> content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructeurs
    public CreateMedicalRecordRequest() {}

    // Getters et Setters
    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

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

    public String getRecordType() {
        return recordType;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    public LocalDateTime getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(LocalDateTime visitDate) {
        this.visitDate = visitDate;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public Map<String, Object> getContent() {
        return content;
    }

    public void setContent(Map<String, Object> content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

