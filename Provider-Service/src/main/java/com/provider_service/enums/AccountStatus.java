package com.provider_service.enums;

public enum AccountStatus {
    PENDING("Account is pending provider approval"),
    ACTIVE("Account is active"),
    INACTIVE("Account has been deactivated"),
    SUSPENDED("Account has been suspended");  // ← Ajouté pour suspension

    private final String description;

    AccountStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
