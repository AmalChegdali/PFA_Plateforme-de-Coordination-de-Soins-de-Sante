package com.patient_service.dto;

import com.patient_service.models.AccountStatus;

public class ProfileStatusResponse {
	 private String patientId;
	    private String email;
	    private AccountStatus accountStatus;
	    private boolean basicProfileComplete;
	    private boolean medicalProfileComplete;
	    private int completionPercentage;
	    private String nextStep;
	    private String message;
	    
	    // What's completed
	    private boolean hasPersonalInfo;
	    private boolean hasContactInfo;
	    private boolean hasAddressInfo;
	    private boolean hasMedicalInfo;
	    private boolean hasEmergencyContact;
	    
	    // Constructors
	    public ProfileStatusResponse() {}
	    
	    public ProfileStatusResponse(String patientId, String email, AccountStatus accountStatus) {
	        this.patientId = patientId;
	        this.email = email;
	        this.accountStatus = accountStatus;
	    }
	    
	    // Getters and Setters
	    public String getPatientId() {
	        return patientId;
	    }
	    
	    public void setPatientId(String patientId) {
	        this.patientId = patientId;
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
	    
	    public boolean isBasicProfileComplete() {
	        return basicProfileComplete;
	    }
	    
	    public void setBasicProfileComplete(boolean basicProfileComplete) {
	        this.basicProfileComplete = basicProfileComplete;
	    }
	    
	    public boolean isMedicalProfileComplete() {
	        return medicalProfileComplete;
	    }
	    
	    public void setMedicalProfileComplete(boolean medicalProfileComplete) {
	        this.medicalProfileComplete = medicalProfileComplete;
	    }
	    
	    public int getCompletionPercentage() {
	        return completionPercentage;
	    }
	    
	    public void setCompletionPercentage(int completionPercentage) {
	        this.completionPercentage = completionPercentage;
	    }
	    
	    public String getNextStep() {
	        return nextStep;
	    }
	    
	    public void setNextStep(String nextStep) {
	        this.nextStep = nextStep;
	    }
	    
	    public String getMessage() {
	        return message;
	    }
	    
	    public void setMessage(String message) {
	        this.message = message;
	    }
	    
	    public boolean isHasPersonalInfo() {
	        return hasPersonalInfo;
	    }
	    
	    public void setHasPersonalInfo(boolean hasPersonalInfo) {
	        this.hasPersonalInfo = hasPersonalInfo;
	    }
	    
	    public boolean isHasContactInfo() {
	        return hasContactInfo;
	    }
	    
	    public void setHasContactInfo(boolean hasContactInfo) {
	        this.hasContactInfo = hasContactInfo;
	    }
	    
	    public boolean isHasAddressInfo() {
	        return hasAddressInfo;
	    }
	    
	    public void setHasAddressInfo(boolean hasAddressInfo) {
	        this.hasAddressInfo = hasAddressInfo;
	    }
	    
	    public boolean isHasMedicalInfo() {
	        return hasMedicalInfo;
	    }
	    
	    public void setHasMedicalInfo(boolean hasMedicalInfo) {
	        this.hasMedicalInfo = hasMedicalInfo;
	    }
	    
	    public boolean isHasEmergencyContact() {
	        return hasEmergencyContact;
	    }
	    
	    public void setHasEmergencyContact(boolean hasEmergencyContact) {
	        this.hasEmergencyContact = hasEmergencyContact;
	    }
}
