package com.provider_service.dto;

public class PatientSyncRequest {
    private String requestId;
    private String providerId;
    private String status;

    public PatientSyncRequest() {}

    public PatientSyncRequest(String requestId, String providerId, String status) {
        this.requestId = requestId;
        this.providerId = providerId;
        this.status = status;
    }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
