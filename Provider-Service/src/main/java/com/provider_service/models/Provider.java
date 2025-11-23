package com.provider_service.models;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;




@Document(collection = "providers")
public class Provider implements UserDetails{
	    @Id
	    private String providerID;
	    
		@Indexed(unique = true)
	    private String email;
	    
	    @JsonIgnore
	    private String password;
	    
	    private Role role = Role.PROVIDER;
	    
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
	    
	    public Provider() {
			super();
		}

		public Provider(String email, String password) {
			super();
			this.email = email;
			this.password = password;
		}
	    
		// UserDetails implementation
	    @Override
	    public Collection<? extends GrantedAuthority> getAuthorities() {
	        return Collections.singletonList(new SimpleGrantedAuthority(role.getAuthority()));
	    }
	    
	    @Override
	    public String getUsername() {
	        return email;
	    }
	    
	    public Role getRole() {
	        return role;
	    }

	    public void setRole(Role role) {
	        this.role = role;
	    }
	    
	    @Override
	    public boolean isAccountNonExpired() {
	        return accountNonExpired;
	    }
	    
	    @Override
	    public boolean isAccountNonLocked() {
	        return accountNonLocked;
	    }
	    
	    @Override
	    public boolean isCredentialsNonExpired() {
	        return credentialsNonExpired;
	    }
	    
	    @Override
	    public boolean isEnabled() {
	        return enabled;
	    }
	    
	    // Getters and Setters
	    public String getId() {
	        return providerID;
	    }
	    
	    public void setId(String providerID) {
	        this.providerID = providerID;
	    }
	    
	    public String getEmail() {
	        return email;
	    }
	    
	    public void setEmail(String email) {
	        this.email = email;
	    }
	    
	    @Override
	    public String getPassword() {
	        return password;
	    }
	    
	    public void setPassword(String password) {
	        this.password = password;
	    }
	    
	    public void setEnabled(boolean enabled) {
	        this.enabled = enabled;
	    }
	    
	    public void setAccountNonExpired(boolean accountNonExpired) {
	        this.accountNonExpired = accountNonExpired;
	    }
	    
	    public void setAccountNonLocked(boolean accountNonLocked) {
	        this.accountNonLocked = accountNonLocked;
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
		
		// Helper method to check if profile is complete
		public boolean isProfileComplete() {
		    return fullName != null &&
		           professionalTitle != null &&
		           specialty != null &&
		           stateLicenses != null && !stateLicenses.isEmpty() &&
		           primaryClinicName != null &&
		           clinicAddress != null &&
		           contactNumber != null;
		}
	    
}
