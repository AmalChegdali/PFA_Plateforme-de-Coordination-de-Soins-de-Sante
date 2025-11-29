package com.medicalrecord_service.dto;

public class UpdateRecordRequest {
    private String notes;
    private String lastUpdatedBy;

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getLastUpdatedBy() { return lastUpdatedBy; }
    public void setLastUpdatedBy(String lastUpdatedBy) { this.lastUpdatedBy = lastUpdatedBy; }
}
