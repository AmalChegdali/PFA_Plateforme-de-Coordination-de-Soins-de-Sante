package com.medicalrecord_service.dto;

import java.time.LocalDateTime;
import java.util.Map;

public class CreateRecordRequest {
    private String patientId;
    private String providerId;
    private String recordType;
    private LocalDateTime visitDate;
    private String diagnosis;
    private Map<String, Object> content;

    // Getters & Setters
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }

    public String getRecordType() { return recordType; }
    public void setRecordType(String recordType) { this.recordType = recordType; }

    public LocalDateTime getVisitDate() { return visitDate; }
    public void setVisitDate(LocalDateTime visitDate) { this.visitDate = visitDate; }

    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

    public Map<String, Object> getContent() { return content; }
    public void setContent(Map<String, Object> content) { this.content = content; }
}
