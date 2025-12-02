package com.patient_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO pour la réponse d'une demande patient.
 */
public class PatientRequestResponseDTO {

    @JsonProperty("requestId")
    private String requestId;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("message")
    private String message;

    // Constructeur par défaut (requis pour Jackson)
    public PatientRequestResponseDTO() {
    }

    // Constructeur avec paramètres
    public PatientRequestResponseDTO(String requestId, String status, String message) {
        this.requestId = requestId;
        this.status = status;
        this.message = message;
    }

    // Getters et Setters (requis pour la sérialisation JSON)
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
}
