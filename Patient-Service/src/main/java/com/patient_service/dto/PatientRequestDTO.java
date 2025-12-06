package com.patient_service.dto;

import java.time.LocalDate;

public class PatientRequestDTO {

    private String type;
    private String priority;
    private String subject;
    private String description;
    private LocalDate preferredDate;
    private String targetProviderId; // ID du provider cible (optionnel)

    // Constructeurs
    public PatientRequestDTO() {}

    public PatientRequestDTO(String type, String priority, String subject, String description, LocalDate preferredDate) {
        this.type = type;
        this.priority = priority;
        this.subject = subject;
        this.description = description;
        this.preferredDate = preferredDate;
    }

    public PatientRequestDTO(String type, String priority, String subject, String description, LocalDate preferredDate, String targetProviderId) {
        this.type = type;
        this.priority = priority;
        this.subject = subject;
        this.description = description;
        this.preferredDate = preferredDate;
        this.targetProviderId = targetProviderId;
    }

    // Getters et Setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getPreferredDate() { return preferredDate; }
    public void setPreferredDate(LocalDate preferredDate) { this.preferredDate = preferredDate; }

    public String getTargetProviderId() { return targetProviderId; }
    public void setTargetProviderId(String targetProviderId) { this.targetProviderId = targetProviderId; }
}
