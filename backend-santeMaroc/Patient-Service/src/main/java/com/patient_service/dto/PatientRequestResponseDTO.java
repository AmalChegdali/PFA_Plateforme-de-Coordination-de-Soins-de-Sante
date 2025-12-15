package com.patient_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * DTO pour la réponse d'une demande patient.
 * Inclut toutes les informations de la demande, y compris le provider cible.
 */
public class PatientRequestResponseDTO {

    @JsonProperty("requestId")
    private String requestId;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("priority")
    private String priority;
    
    @JsonProperty("subject")
    private String subject;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("preferredDate")
    private String preferredDate;
    
    @JsonProperty("targetProviderId")
    private String targetProviderId; // Provider cible (si spécifié)
    
    @JsonProperty("targetProviderName")
    private String targetProviderName; // Nom du provider cible (optionnel)
    
    @JsonProperty("providerId")
    private String providerId; // Provider qui a traité la demande
    
    @JsonProperty("providerName")
    private String providerName; // Nom du provider qui a traité
    
    @JsonProperty("responseMessage")
    private String responseMessage; // Message de réponse du provider
    
    @JsonProperty("responseDate")
    private LocalDateTime responseDate;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;

    @JsonProperty("patientId")
    private String patientId; // ✅ Ajouté pour identifier le patient

    // Constructeur par défaut (requis pour Jackson)
    public PatientRequestResponseDTO() {
    }

    // Constructeur simple (pour compatibilité)
    public PatientRequestResponseDTO(String requestId, String status, String message) {
        this.requestId = requestId;
        this.status = status;
        this.message = message;
    }

    // Getters et Setters
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPreferredDate() {
        return preferredDate;
    }

    public void setPreferredDate(String preferredDate) {
        this.preferredDate = preferredDate;
    }

    public String getTargetProviderId() {
        return targetProviderId;
    }

    public void setTargetProviderId(String targetProviderId) {
        this.targetProviderId = targetProviderId;
    }

    public String getTargetProviderName() {
        return targetProviderName;
    }

    public void setTargetProviderName(String targetProviderName) {
        this.targetProviderName = targetProviderName;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public LocalDateTime getResponseDate() {
        return responseDate;
    }

    public void setResponseDate(LocalDateTime responseDate) {
        this.responseDate = responseDate;
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

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }
}
