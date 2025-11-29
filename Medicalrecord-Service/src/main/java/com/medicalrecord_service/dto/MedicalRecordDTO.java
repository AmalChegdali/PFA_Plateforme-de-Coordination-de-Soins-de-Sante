package com.medicalrecord_service.dto;

import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
public class MedicalRecordDTO {
    private String recordId;
    private String patientId;
    private String providerId;
    private String recordType;
    private Instant visitDate;
    private String diagnosis;
    private Map<String, String> content;
    private String notes;
    private boolean active;
}
