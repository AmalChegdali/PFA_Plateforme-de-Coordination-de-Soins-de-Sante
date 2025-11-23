package com.provider_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientSyncRequest {
    private String requestId;
    private String providerId;
    private String status; // "ALL", "PENDING", "ACTIVE", "INACTIVE"
}

