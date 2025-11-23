package com.provider_service.models;

public enum Role {
	PROVIDER("ROLE_PROVIDER");
    
    
    private final String authority;
    
    Role(String authority) {
        this.authority = authority;
    }
    
    public String getAuthority() {
        return authority;
    }
}
