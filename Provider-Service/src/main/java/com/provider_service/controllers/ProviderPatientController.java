package com.provider_service.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.provider_service.dto.PatientDTO;
import com.provider_service.services.ProviderPatientService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/providers")
@RequiredArgsConstructor
@Slf4j
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
            @PathVariable(value = "patientId") String patientId,
            Authentication authentication) {
        
        String providerId = authentication.getName();
        
        List<PatientDTO> patients = providerPatientService.getPatients(providerId, "ALL");
        PatientDTO patient = patients.stream()
                .filter(p -> p.getId().equals(patientId))
                .findFirst()
                .orElse(null);
        
        return patient != null ? 
                ResponseEntity.ok(patient) : 
                ResponseEntity.notFound().build();
    }

    @PostMapping("/patients/{patientId}/activate")
    public ResponseEntity<Map<String, String>> activatePatient(
            @PathVariable(value = "patientId") String patientId,
            @RequestBody(required = false) Map<String, String> request,
            Authentication authentication) {
        
        String providerId = authentication.getName();
        String reason = request != null ? 
                request.getOrDefault("reason", "Approved by provider") : 
                "Approved by provider";
        
        try {
            providerPatientService.activatePatient(patientId, providerId, reason);
            
            return ResponseEntity.ok(Map.of(
                    "message", "Patient activation request sent successfully",
                    "patientId", patientId,
                    "status", "PROCESSING"
            ));
        } catch (Exception e) {
            log.error("Error activating patient {}: {}", patientId, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to process activation request"));
        }
    }

    @PostMapping("/patients/{patientId}/deactivate")
    public ResponseEntity<Map<String, String>> deactivatePatient(
            @PathVariable(value = "patientId") String patientId,
            @RequestBody(required = false) Map<String, String> request,
            Authentication authentication) {
        
        String providerId = authentication.getName();
        String reason = request != null ? 
                request.getOrDefault("reason", "Deactivated by provider") : 
                "Deactivated by provider";
        
        try {
            providerPatientService.deactivatePatient(patientId, providerId, reason);
            
            return ResponseEntity.ok(Map.of(
                    "message", "Patient deactivation request sent successfully",
                    "patientId", patientId,
                    "status", "PROCESSING"
            ));
        } catch (Exception e) {
            log.error("Error deactivating patient {}: {}", patientId, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to process deactivation request"));
        }
    }
}
