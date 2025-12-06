package com.provider_service.controllers;

import com.provider_service.dto.PatientDTO;
import com.provider_service.enums.AccountStatus;
import com.provider_service.services.ProviderPatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/providers")
@RequiredArgsConstructor
@Tag(name = "Patient Management", description = "Endpoints pour gérer les patients")
public class ProviderPatientController {

    private final ProviderPatientService providerPatientService;

    @GetMapping("/patients/all")
    @Operation(summary = "Récupérer tous les patients", 
               description = "Retourne la liste complète de tous les patients, quel que soit leur statut. " +
                           "Si la liste est vide, une synchronisation automatique sera déclenchée. " +
                           "Si la liste est toujours vide après l'appel, attendez quelques secondes et réessayez.")
    public ResponseEntity<?> getAllPatients(Authentication authentication) {
        String providerId = authentication.getName();
        List<PatientDTO> patients = providerPatientService.getPatients(providerId, "ALL");
        
        // Si la liste est vide, indiquer qu'une synchronisation est peut-être en cours
        if (patients.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                "message", "Aucun patient trouvé. Une synchronisation automatique a été déclenchée. " +
                          "Veuillez réessayer dans quelques secondes ou appeler POST /api/providers/patients/sync",
                "patients", patients,
                "syncRecommended", true
            ));
        }
        
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/patients")
    @Operation(summary = "Récupérer les patients filtrés par statut", 
               description = "Retourne la liste des patients filtrés par statut (PENDING, ACTIVE, SUSPENDED, ou ALL)")
    public ResponseEntity<List<PatientDTO>> getPatients(
            @RequestParam(value = "status", defaultValue = "PENDING") String status,
            Authentication authentication) {

        String providerId = authentication.getName();
        List<PatientDTO> patients = providerPatientService.getPatients(providerId, status);
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/patients/{patientId}")
    @Operation(summary = "Récupérer un patient par ID", 
               description = "Retourne les détails d'un patient spécifique par son ID")
    public ResponseEntity<PatientDTO> getPatient(
            @PathVariable String patientId,
            Authentication authentication) {

        String providerId = authentication.getName();
        PatientDTO patient = providerPatientService.getPatientById(providerId, patientId);
        return patient != null ? ResponseEntity.ok(patient) : ResponseEntity.notFound().build();
    }

    @PutMapping("/patients/{patientId}/status")
    @Operation(summary = "Mettre à jour le statut d'un patient", 
               description = "Met à jour le statut d'un patient (ACTIVE, SUSPENDED, etc.)")
    public ResponseEntity<Void> updatePatientStatus(
            @PathVariable String patientId,
            @RequestParam AccountStatus status,
            Authentication authentication) {

        String providerId = authentication.getName();
        providerPatientService.updatePatientStatus(patientId, status, providerId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/patients/sync")
    @Operation(summary = "Synchroniser tous les patients", 
               description = "Demande la synchronisation de tous les patients existants depuis Patient-Service")
    public ResponseEntity<Map<String, String>> syncAllPatients(Authentication authentication) {
        String providerId = authentication.getName();
        try {
            providerPatientService.requestSyncAllPatients(providerId);
            return ResponseEntity.ok(Map.of(
                "message", "Demande de synchronisation envoyée avec succès",
                "status", "success"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "message", "Erreur lors de la synchronisation : " + e.getMessage(),
                "status", "error"
            ));
        }
    }
}
