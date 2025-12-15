package com.patient_service.services;

import com.patient_service.dto.PatientDTO;
import com.patient_service.models.Patient;
import org.springframework.stereotype.Component;

@Component
public class PatientMapper {

    // Convertir Patient -> PatientDTO
    public PatientDTO toDto(Patient patient) {
        if (patient == null) return null;

        PatientDTO dto = new PatientDTO();
        dto.setId(patient.getId());
        dto.setFirstName(patient.getFirstName());
        dto.setLastName(patient.getLastName());
        dto.setEmail(patient.getEmail());
        dto.setPhone(patient.getPhone());
        dto.setDateOfBirth(patient.getDateOfBirth());
        dto.setGender(patient.getGender());
        dto.setAddress(patient.getAddress());
        dto.setCity(patient.getCity());
        dto.setState(patient.getState());
        dto.setZipCode(patient.getZipCode());
        dto.setCountry(patient.getCountry());
        dto.setAccountStatus(patient.getAccountStatus());
        dto.setCreatedAt(patient.getCreatedAt());
        dto.setUpdatedAt(patient.getUpdatedAt());

        return dto;
    }

    // Optionnel : Convertir PatientDTO -> Patient
    public Patient toEntity(PatientDTO dto) {
        if (dto == null) return null;

        Patient patient = new Patient();
        patient.setId(dto.getId());
        patient.setFirstName(dto.getFirstName());
        patient.setLastName(dto.getLastName());
        patient.setEmail(dto.getEmail());
        patient.setPhone(dto.getPhone());
        patient.setDateOfBirth(dto.getDateOfBirth());
        patient.setGender(dto.getGender());
        patient.setAddress(dto.getAddress());
        patient.setCity(dto.getCity());
        patient.setState(dto.getState());
        patient.setZipCode(dto.getZipCode());
        patient.setCountry(dto.getCountry());
        patient.setAccountStatus(dto.getAccountStatus());
        patient.setCreatedAt(dto.getCreatedAt());
        patient.setUpdatedAt(dto.getUpdatedAt());

        return patient;
    }
}
