package com.provider_service.dto;

import com.provider_service.enums.AccountStatus;

public class PatientDTO {
    private String id;
    private String fullName;
    private String email;
    private String phone;
    private AccountStatus accountStatus;

    public PatientDTO() {}

    public PatientDTO(String id, String fullName, String email, String phone, AccountStatus accountStatus) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.accountStatus = accountStatus;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public AccountStatus getAccountStatus() { return accountStatus; }
    public void setAccountStatus(AccountStatus accountStatus) { this.accountStatus = accountStatus; }
}
