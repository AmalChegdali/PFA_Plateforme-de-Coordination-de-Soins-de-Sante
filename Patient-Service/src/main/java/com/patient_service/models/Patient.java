package com.patient_service.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.patient_service.enums.Role;
import com.patient_service.enums.AccountStatus;

/**
 * Classe principale représentant un patient.
 * Implémente UserDetails pour Spring Security.
 */
@Document(collection = "patients")
public class Patient implements UserDetails {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    @JsonIgnore
    private String password;

    private Role role = Role.PATIENT;

    private boolean enabled = true;
    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true;
    private boolean credentialsNonExpired = true;

    private AccountStatus accountStatus;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    private PersonalInfo personalInfo;
    private Address addressInfo;

    // ---------- Constructeurs ----------
    public Patient() {
        this.personalInfo = new PersonalInfo();
        this.addressInfo = new Address();
    }

    public Patient(String email, String password) {
        this();
        this.email = email;
        this.password = password;
    }

    // ---------- UserDetails methods ----------
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role.getAuthority()));
    }

    @Override
    public String getUsername() { return email; }

    @Override
    public String getPassword() { return password; }

    @Override
    public boolean isAccountNonExpired() { return accountNonExpired; }

    @Override
    public boolean isAccountNonLocked() { return accountNonLocked; }

    @Override
    public boolean isCredentialsNonExpired() { return credentialsNonExpired; }

    @Override
    public boolean isEnabled() { return enabled; }

    // ---------- Getters / Setters ----------
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public AccountStatus getAccountStatus() { return accountStatus; }
    public void setAccountStatus(AccountStatus accountStatus) {
        this.accountStatus = accountStatus;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public PersonalInfo getPersonalInfo() { return personalInfo; }
    public Address getAddressInfo() { return addressInfo; }

    // ---------- PersonalInfo delegated getters/setters ----------
    public String getFirstName() { return personalInfo.getFirstName(); }
    public void setFirstName(String firstName) { personalInfo.setFirstName(firstName); }

    public String getLastName() { return personalInfo.getLastName(); }
    public void setLastName(String lastName) { personalInfo.setLastName(lastName); }

    public String getPhone() { return personalInfo.getPhone(); }
    public void setPhone(String phone) { personalInfo.setPhone(phone); }

    public String getGender() { return personalInfo.getGender(); }
    public void setGender(String gender) { personalInfo.setGender(gender); }

    // Dans Patient.java
    private LocalDate dateOfBirth;  // au lieu de LocalDateTime

    public LocalDate getDateOfBirth() { return personalInfo.getDateOfBirth(); }
    public void setDateOfBirth(LocalDate dateOfBirth) { personalInfo.setDateOfBirth(dateOfBirth); }

    // ---------- Address delegated getters/setters ----------
    public String getAddress() { return addressInfo.getAddress(); }
    public void setAddress(String address) { addressInfo.setAddress(address); }

    public String getCity() { return addressInfo.getCity(); }
    public void setCity(String city) { addressInfo.setCity(city); }

    public String getState() { return addressInfo.getState(); }
    public void setState(String state) { addressInfo.setState(state); }

    public String getZipCode() { return addressInfo.getZipCode(); }
    public void setZipCode(String zipCode) { addressInfo.setZipCode(zipCode); }

    public String getCountry() { return addressInfo.getCountry(); }
    public void setCountry(String country) { addressInfo.setCountry(country); }

    // ---------- Helper ----------
    public String getFullName() {
        if(personalInfo.getFirstName() != null && personalInfo.getLastName() != null) {
            return personalInfo.getFirstName() + " " + personalInfo.getLastName();
        }
        return email;
    }
}
