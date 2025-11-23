package com.patient_service.models;

public enum Role {
    PATIENT("ROLE_PATIENT");
    
    
    private final String authority;
    
    Role(String authority) {
        this.authority = authority;
    }
    
    public String getAuthority() {
        return authority;
    }
}
