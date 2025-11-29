package com.provider_service.dto;

public class AuthResponse {

    private String token;
    private String message;
    private String email;
    private String role;

    // ✅ Constructeur vide (obligatoire pour Jackson)
    public AuthResponse() {
    }

    // ✅ Constructeur sans role
    public AuthResponse(String token, String message, String email) {
        this.token = token;
        this.message = message;
        this.email = email;
    }

    // ✅ Constructeur complet AVEC role
    public AuthResponse(String token, String message, String email, String role) {
        this.token = token;
        this.message = message;
        this.email = email;
        this.role = role;
    }

    // ✅ Getters & Setters
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
