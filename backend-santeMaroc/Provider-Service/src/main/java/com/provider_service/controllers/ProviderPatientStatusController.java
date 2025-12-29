package com.provider_service.controllers;

import com.provider_service.dto.PatientDTO;
import com.provider_service.models.Provider;
import com.provider_service.services.ProviderPatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/providers/patients")
@RequiredArgsConstructor
public class ProviderPatientStatusController {

    private final ProviderPatientService providerPatientService;

    /**
     * Active un patient.
     * 
     * @param patientId L'ID du patient à activer
     * @param authentication L'authentification du provider
     * @return Le patient activé
     */
    @PostMapping("/{patientId}/activate")
    public ResponseEntity<PatientDTO> activatePatient(
            @PathVariable String patientId,
            Authentication authentication) {
        Provider provider = (Provider) authentication.getPrincipal();
        PatientDTO updatedPatient = providerPatientService.activatePatient(patientId, provider.getId());
        return ResponseEntity.ok(updatedPatient);
    }

    /**
     * Suspend un patient avec une raison.
     * 
     * @param patientId L'ID du patient à suspendre
     * @param payload Le payload contenant la raison
     * @param authentication L'authentification du provider
     * @return Le patient suspendu
     */
    @PostMapping("/{patientId}/suspend")
    public ResponseEntity<PatientDTO> suspendPatient(
            @PathVariable String patientId,
            @RequestBody Map<String, String> payload,
            Authentication authentication) {

        String reason = payload.get("reason");
        Provider provider = (Provider) authentication.getPrincipal();
        PatientDTO updatedPatient = providerPatientService.suspendPatient(patientId, reason, provider.getId());
        return ResponseEntity.ok(updatedPatient);
    }
}
