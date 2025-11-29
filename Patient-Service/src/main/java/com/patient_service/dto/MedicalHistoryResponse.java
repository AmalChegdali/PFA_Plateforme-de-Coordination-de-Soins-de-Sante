package com.patient_service.dto;

import java.time.LocalDateTime;

public class MedicalHistoryResponse {

    private String id;
    private String providerName;
    private String visitType;
    private LocalDateTime visitDate;
    private String diagnosis;
    private String prescription;
    private String notes;

    // ✅ Getters & Setters
}
