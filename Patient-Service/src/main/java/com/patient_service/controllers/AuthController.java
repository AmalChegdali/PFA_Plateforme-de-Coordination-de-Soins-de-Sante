package com.patient_service.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.patient_service.dto.AuthRequest;
import com.patient_service.dto.AuthResponse;
import com.patient_service.dto.PatientProfileDTO;
import com.patient_service.dto.ProfileStatusResponse;
import com.patient_service.dto.RegisterRequest;
import com.patient_service.enums.AccountStatus;
import com.patient_service.models.Patient;
import com.patient_service.services.JwtService;
import com.patient_service.services.PatientService;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@Tag(name = "Authentication", description = "Endpoints for patient authentication and profile management")
public class AuthController {

    @Autowired
    private PatientService patientService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/register")
    @Operation(summary = "Register a new patient", description = "Registers a patient and returns JWT token with account status")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            Patient patient = patientService.registerPatient(request.getEmail(), request.getPassword(), request);
            String token = jwtService.generateToken(patient);

            AuthResponse response = new AuthResponse(
                    token,
                    "Registration successful. Your account is pending provider approval.",
                    patient.getEmail(),
                    patient.getAccountStatus(),
                    patientService.canAccessMedicalHistory(patient),
                    patient.getRole().getAuthority()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new AuthResponse(null, "Registration failed: " + e.getMessage(), null));
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate patient", description = "Authenticates a patient and returns JWT token and account information")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            Patient patient = (Patient) authentication.getPrincipal();
            String token = jwtService.generateToken(patient);

            String message = patient.getAccountStatus() == AccountStatus.ACTIVE
                    ? "Login successful"
                    : "Login successful. " + patient.getAccountStatus().getDescription();

            AuthResponse response = new AuthResponse(
                    token,
                    message,
                    patient.getEmail(),
                    patient.getAccountStatus(),
                    patientService.canAccessMedicalHistory(patient),
                    patient.getRole().getAuthority()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new AuthResponse(null, "Invalid credentials", null));
        }
    }

    @GetMapping("/profile-status")
    @Operation(summary = "Get profile status", description = "Returns the profile completion status of the logged-in patient")
    public ResponseEntity<ProfileStatusResponse> getProfileStatus(Authentication authentication) {
        try {
            Patient patient = (Patient) authentication.getPrincipal();
            ProfileStatusResponse status = patientService.getProfileStatus(patient.getId());
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/profile")
    @Operation(summary = "Get patient profile", description = "Returns the full profile of the logged-in patient")
    public ResponseEntity<PatientProfileDTO> getProfile(Authentication authentication) {
        try {
            Patient patient = (Patient) authentication.getPrincipal();
            patient = patientService.findById(patient.getId());
            return ResponseEntity.ok(convertToProfileDTO(patient));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/complete-profile")
    @Operation(summary = "Update patient profile", description = "Updates the profile information of the logged-in patient")
    public ResponseEntity<PatientProfileDTO> updateProfile(@Valid @RequestBody Patient profileUpdates,
                                                           Authentication authentication) {
        try {
            Patient currentPatient = (Patient) authentication.getPrincipal();
            Patient updatedPatient = patientService.updatePatientProfile(currentPatient.getId(), profileUpdates);
            return ResponseEntity.ok(convertToProfileDTO(updatedPatient));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ---------- Helper method ----------
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
