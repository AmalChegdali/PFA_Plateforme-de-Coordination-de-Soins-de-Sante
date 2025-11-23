package com.provider_service.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;

public class ProfileCompletionRequest {
	
    @NotBlank(message = "fullName is required")
    private String fullName;
    
    private String professionalTitle;
    private String specialty;
    private List<String> subSpecialties;
    private List<String> stateLicenses;
    
    private String primaryClinicName;
    private String clinicAddress;
    private String contactNumber;
    
	public ProfileCompletionRequest() {
		super();
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getProfessionalTitle() {
		return professionalTitle;
	}

	public void setProfessionalTitle(String professionalTitle) {
		this.professionalTitle = professionalTitle;
	}

	public String getSpecialty() {
		return specialty;
	}

	public void setSpecialty(String specialty) {
		this.specialty = specialty;
	}

	public List<String> getSubSpecialties() {
		return subSpecialties;
	}

	public void setSubSpecialties(List<String> subSpecialties) {
		this.subSpecialties = subSpecialties;
	}

	public List<String> getStateLicenses() {
		return stateLicenses;
	}

	public void setStateLicenses(List<String> stateLicenses) {
		this.stateLicenses = stateLicenses;
	}

	public String getPrimaryClinicName() {
		return primaryClinicName;
	}

	public void setPrimaryClinicName(String primaryClinicName) {
		this.primaryClinicName = primaryClinicName;
	}

	public String getClinicAddress() {
		return clinicAddress;
	}

	public void setClinicAddress(String clinicAddress) {
		this.clinicAddress = clinicAddress;
	}

	public String getContactNumber() {
		return contactNumber;
	}

	public void setContactNumber(String contactNumber) {
		this.contactNumber = contactNumber;
	}
    
	
    
}
