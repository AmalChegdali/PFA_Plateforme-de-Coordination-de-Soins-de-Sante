package com.provider_service.dto;

import com.provider_service.enums.AccountStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PatientDTO {

    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phone;
    private String gender;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    private AccountStatus accountStatus;
    private LocalDate dateOfBirth;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Nouveau champ pour la suspension
    private String suspensionReason;
    
    // Champ pour l'assignation à un provider
    private String assignedProviderId; // ID du provider assigné à ce patient (null si non assigné)

    // Constructeur vide
    public PatientDTO() {}

    // Constructeur utilisé par le listener
    public PatientDTO(String id, String fullName, String email, String phone, AccountStatus accountStatus) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.accountStatus = accountStatus;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public AccountStatus getAccountStatus() { return accountStatus; }
    public void setAccountStatus(AccountStatus accountStatus) { this.accountStatus = accountStatus; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // ✅ Getter & Setter pour suspensionReason
    public String getSuspensionReason() { return suspensionReason; }
    public void setSuspensionReason(String suspensionReason) { this.suspensionReason = suspensionReason; }
    
    // ✅ Getter & Setter pour assignedProviderId
    public String getAssignedProviderId() { return assignedProviderId; }
    public void setAssignedProviderId(String assignedProviderId) { this.assignedProviderId = assignedProviderId; }
}
