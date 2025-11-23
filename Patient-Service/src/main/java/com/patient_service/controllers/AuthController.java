package com.patient_service.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.patient_service.dto.AuthRequest;
import com.patient_service.dto.AuthResponse;
import com.patient_service.dto.PatientProfileDTO;
import com.patient_service.dto.ProfileStatusResponse;
import com.patient_service.dto.RegisterRequest;
import com.patient_service.models.AccountStatus;
import com.patient_service.models.Patient;
import com.patient_service.services.JwtService;
import com.patient_service.services.PatientService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
	@Autowired
    private PatientService patientService;
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            // Encode password before saving
            
            Patient patient = patientService.registerPatient(request.getEmail(), request.getPassword(), request);
            String token = jwtService.generateToken(patient);
            
            // Create enhanced response with account status
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
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            
            Patient patient = (Patient) authentication.getPrincipal();
            String token = jwtService.generateToken(patient);
            
            // Create enhanced response with account status
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
    public ResponseEntity<ProfileStatusResponse> getProfileStatus(Authentication authentication) {
        try {
            Patient patient = (Patient) authentication.getPrincipal();
            ProfileStatusResponse status = patientService.getProfileStatus(patient.getId());
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    @GetMapping("/profile")
    public ResponseEntity<PatientProfileDTO> getProfile(Authentication authentication) {
        try {
            Patient patient = (Patient) authentication.getPrincipal();
            // Refresh patient data from database
            patient = patientService.findById(patient.getId());
            
            PatientProfileDTO profileDTO = convertToProfileDTO(patient);
            return ResponseEntity.ok(profileDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    @PutMapping("/complete-profile")
    public ResponseEntity<PatientProfileDTO> updateProfile(
            @RequestBody Patient profileUpdates, 
            Authentication authentication) {
        try {
            Patient currentPatient = (Patient) authentication.getPrincipal();
            
            Patient updatedPatient = patientService.updatePatientProfile(currentPatient.getId(), profileUpdates);
            PatientProfileDTO profileDTO = convertToProfileDTO(updatedPatient);
            
            return ResponseEntity.ok(profileDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    
    private PatientProfileDTO convertToProfileDTO(Patient patient) {
        PatientProfileDTO dto = new PatientProfileDTO();
        dto.setId(patient.getId());
        dto.setEmail(patient.getEmail());
        //dto.setRole(patient.getRole());
        dto.setAccountStatus(patient.getAccountStatus());
        dto.setFirstName(patient.getFirstName());
        dto.setLastName(patient.getLastName());
        dto.setPhone(patient.getPhone());
        dto.setDateOfBirth(patient.getDateOfBirth());
        dto.setGender(patient.getGender());
        dto.setAddress(patient.getAddress());
        dto.setCity(patient.getCity());
        dto.setState(patient.getState());
        dto.setZipCode(patient.getZipCode());
        dto.setCountry(patient.getCountry());
        dto.setCreatedAt(patient.getCreatedAt());
        dto.setUpdatedAt(patient.getUpdatedAt());
        return dto;
    }
}
