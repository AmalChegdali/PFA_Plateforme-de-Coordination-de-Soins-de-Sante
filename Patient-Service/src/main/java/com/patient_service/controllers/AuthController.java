package com.patient_service.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.patient_service.dto.AuthRequest;
import com.patient_service.dto.AuthResponse;
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
@Tag(name = "Authentication", description = "Patient authentication endpoints")
public class AuthController {

    @Autowired
    private PatientService patientService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    // ✅ REGISTER
    @PostMapping("/register")
    @Operation(summary = "Register a new patient")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            Patient patient = patientService.registerPatient(
                    request.getEmail(),
                    request.getPassword(),
                    request
            );

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
            return ResponseEntity
                    .badRequest()
                    .body(new AuthResponse(null, "Registration failed: " + e.getMessage(), null));
        }
    }

    // ✅ LOGIN
    @PostMapping("/login")
    @Operation(summary = "Authenticate patient")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
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
            return ResponseEntity
                    .badRequest()
                    .body(new AuthResponse(null, "Invalid credentials", null));
        }
    }
}
