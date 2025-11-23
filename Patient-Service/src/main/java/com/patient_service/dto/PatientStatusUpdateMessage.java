package com.patient_service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientStatusUpdateMessage {
    private String patientId;
    private String providerId;
    private String newStatus;
    private String previousStatus;
    private String reason;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    public PatientStatusUpdateMessage(String patientId, String providerId, 
                                    String newStatus, String previousStatus) {
        this.patientId = patientId;
        this.providerId = providerId;
        this.newStatus = newStatus;
        this.previousStatus = previousStatus;
        this.timestamp = LocalDateTime.now();
    }
}
