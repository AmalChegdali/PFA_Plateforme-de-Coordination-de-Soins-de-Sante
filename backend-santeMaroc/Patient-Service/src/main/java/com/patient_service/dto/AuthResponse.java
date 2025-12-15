package com.patient_service.dto;

import com.patient_service.enums.AccountStatus;

public class AuthResponse {
    private String token;
    private String message;
    private String email;
    private AccountStatus accountStatus;         // ajouter accountStatus
    private boolean canAccessMedicalHistory;     // ajouter canAccessMedicalHistory
    private String role;

    // ---------- Constructeurs ----------
    public AuthResponse() {}

    public AuthResponse(String token, String message, String email) {
        this.token = token;
        this.message = message;
        this.email = email;
    }

    public AuthResponse(String token, String message, String email, AccountStatus accountStatus,
                        boolean canAccessMedicalHistory, String role) {
        this.token = token;
        this.message = message;
        this.email = email;
        this.accountStatus = accountStatus;
        this.canAccessMedicalHistory = canAccessMedicalHistory;
        this.role = role;
    }

    // ---------- Getters et Setters ----------
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(AccountStatus accountStatus) {
        this.accountStatus = accountStatus;
    }

    public boolean isCanAccessMedicalHistory() {
        return canAccessMedicalHistory;
    }

    public void setCanAccessMedicalHistory(boolean canAccessMedicalHistory) {
        this.canAccessMedicalHistory = canAccessMedicalHistory;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
