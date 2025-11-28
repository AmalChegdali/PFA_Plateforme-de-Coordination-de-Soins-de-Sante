package com.provider_service.dto;

import com.provider_service.enums.AccountStatus;

public class PatientStatusUpdateMessage {
    private String patientId;
    private String providerId;
    private AccountStatus oldStatus;
    private AccountStatus newStatus;

    public PatientStatusUpdateMessage() {}

    public PatientStatusUpdateMessage(String patientId, String providerId, AccountStatus oldStatus, AccountStatus newStatus) {
        this.patientId = patientId;
        this.providerId = providerId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }

    public AccountStatus getOldStatus() { return oldStatus; }
    public void setOldStatus(AccountStatus oldStatus) { this.oldStatus = oldStatus; }

    public AccountStatus getNewStatus() { return newStatus; }
    public void setNewStatus(AccountStatus newStatus) { this.newStatus = newStatus; }
}
