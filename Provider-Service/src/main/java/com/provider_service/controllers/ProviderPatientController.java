package com.provider_service.controllers;

import com.provider_service.dto.PatientDTO;
import com.provider_service.enums.AccountStatus;
import com.provider_service.services.ProviderPatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/providers")
@RequiredArgsConstructor
public class ProviderPatientController {

    private final ProviderPatientService providerPatientService;

    @GetMapping("/patients")
    public ResponseEntity<List<PatientDTO>> getPatients(
            @RequestParam(value = "status", defaultValue = "PENDING") String status,
            Authentication authentication) {

        String providerId = authentication.getName();
        List<PatientDTO> patients = providerPatientService.getPatients(providerId, status);
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/patients/{patientId}")
    public ResponseEntity<PatientDTO> getPatient(
            @PathVariable String patientId,
            Authentication authentication) {

        String providerId = authentication.getName();
        PatientDTO patient = providerPatientService.getPatientById(providerId, patientId);
        return patient != null ? ResponseEntity.ok(patient) : ResponseEntity.notFound().build();
    }

    @PutMapping("/patients/{patientId}/status")
    public ResponseEntity<Void> updatePatientStatus(
            @PathVariable String patientId,
            @RequestParam AccountStatus status,
            Authentication authentication) {

        String providerId = authentication.getName();
        providerPatientService.updatePatientStatus(patientId, status, providerId);
        return ResponseEntity.ok().build();
    }
}
