package com.patient_service.enums;

public enum Role {
    PATIENT,
    DOCTOR,
    ADMIN;

    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}
