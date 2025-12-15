package com.provider_service.dto;

import java.util.List;


public class ProviderProfileDTO {
	 
	    private String providerID;
	    
	    private String email;
	    
	    private String password;
	    
	    private boolean enabled = true;
	    private boolean accountNonExpired = true;
	    private boolean accountNonLocked = true;
	    private boolean credentialsNonExpired = true;
	    
	 // Core Professional Information
	    private String fullName;
	    private String professionalTitle;
	    private String specialty;
	    private List<String> subSpecialties;
	    private List<String> stateLicenses;
	    
	 // Practice and Contact Information
	    private String primaryClinicName;
	    private String clinicAddress;
	    private String contactNumber;
	    
		public ProviderProfileDTO() {
			super();
		}

		public String getProviderID() {
			return providerID;
		}

		public void setProviderID(String providerID) {
			this.providerID = providerID;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public boolean isAccountNonExpired() {
			return accountNonExpired;
		}

		public void setAccountNonExpired(boolean accountNonExpired) {
			this.accountNonExpired = accountNonExpired;
		}

		public boolean isAccountNonLocked() {
			return accountNonLocked;
		}

		public void setAccountNonLocked(boolean accountNonLocked) {
			this.accountNonLocked = accountNonLocked;
		}

		public boolean isCredentialsNonExpired() {
			return credentialsNonExpired;
		}

		public void setCredentialsNonExpired(boolean credentialsNonExpired) {
			this.credentialsNonExpired = credentialsNonExpired;
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
