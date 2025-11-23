package com.patient_service.dto;

import com.patient_service.models.AccountStatus;

public class AuthResponse {
	    private String token;
	    private String message;
	    private String email;
	    private AccountStatus accountStatus;
	    private boolean canAccessMedicalHistory;
	    
	    private String role;
	    
	    // Constructors
	    public AuthResponse() {}
	    
	   
	    
	    public AuthResponse(String token, String message, String email) {
			super();
			this.token = token;
			this.message = message;
			this.email = email;
		}



		public AuthResponse(String token, String message, String email, AccountStatus accountStatus,
				boolean canAccessMedicalHistory, String role) {
			super();
			this.token = token;
			this.message = message;
			this.email = email;
			this.accountStatus = accountStatus;
			this.canAccessMedicalHistory = canAccessMedicalHistory;
			this.role = role;
		}



		// Getters and Setters
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
