package com.patient_service.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.patient_service.dto.PatientProfileDTO;
import com.patient_service.dto.ProfileStatusResponse;
import com.patient_service.models.Patient;
import com.patient_service.services.PatientService;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/patient")
@CrossOrigin(origins = "*")
@Tag(name = "Patient Profile", description = "Patient profile management")
public class PatientProfileController {

    @Autowired
    private PatientService patientService;

    // ✅ PROFILE STATUS
    @GetMapping("/profile-status")
    @Operation(summary = "Get profile status")
    public ResponseEntity<ProfileStatusResponse> getProfileStatus(Authentication authentication) {
        try {
            Patient patient = (Patient) authentication.getPrincipal();
            ProfileStatusResponse status = patientService.getProfileStatus(patient.getId());
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ✅ GET PROFILE
    @GetMapping("/profile")
    @Operation(summary = "Get patient profile")
    public ResponseEntity<PatientProfileDTO> getProfile(Authentication authentication) {
        try {
            Patient patient = (Patient) authentication.getPrincipal();
            patient = patientService.findById(patient.getId());
            return ResponseEntity.ok(convertToProfileDTO(patient));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ✅ UPDATE PROFILE
    @PutMapping("/complete-profile")
    @Operation(summary = "Update patient profile")
    public ResponseEntity<PatientProfileDTO> updateProfile(
            @Valid @RequestBody Patient profileUpdates,
            Authentication authentication
    ) {
        try {
            Patient currentPatient = (Patient) authentication.getPrincipal();
            Patient updatedPatient = patientService.updatePatientProfile(
                    currentPatient.getId(),
                    profileUpdates
            );

            return ResponseEntity.ok(convertToProfileDTO(updatedPatient));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ✅ DTO MAPPER
    private PatientProfileDTO convertToProfileDTO(Patient patient) {
        PatientProfileDTO dto = new PatientProfileDTO();

        dto.setId(patient.getId());
        dto.setEmail(patient.getEmail());
        dto.setAccountStatus(patient.getAccountStatus());

        dto.setFirstName(patient.getPersonalInfo().getFirstName());
        dto.setLastName(patient.getPersonalInfo().getLastName());
        dto.setPhone(patient.getPersonalInfo().getPhone());
        dto.setDateOfBirth(patient.getPersonalInfo().getDateOfBirth());
        dto.setGender(patient.getPersonalInfo().getGender());

        dto.setAddress(patient.getAddressInfo().getAddress());
        dto.setCity(patient.getAddressInfo().getCity());
        dto.setState(patient.getAddressInfo().getState());
        dto.setZipCode(patient.getAddressInfo().getZipCode());
        dto.setCountry(patient.getAddressInfo().getCountry());

        dto.setCreatedAt(patient.getCreatedAt());
        dto.setUpdatedAt(patient.getUpdatedAt());

        return dto;
    }
}
