package com.medicalrecord_service.controllers;

import com.medicalrecord_service.models.MedicalRecord;
import com.medicalrecord_service.services.MedicalRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Contrôleur REST pour les opérations de lecture sur les dossiers médicaux.
 * 
 * Ce contrôleur gère :
 * - La récupération des dossiers d'un patient spécifique
 * - La recherche avancée avec plusieurs critères (patient, provider, dates, limite)
 * 
 * Toutes les opérations sont en lecture seule et accessibles à tous les utilisateurs authentifiés.
 * 
 * @author MedicalRecord-Service Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/records/read")
@RequiredArgsConstructor
@Tag(name = "Medical Records - Read Operations", description = "API pour rechercher et lire des dossiers médicaux")
public class MedicalRecordReadController {

    // ==================== CHAMPS ====================
    
    /** Service pour la gestion des dossiers médicaux */
    private final MedicalRecordService service;

    // ==================== ENDPOINTS DE RECHERCHE ====================
    
    /**
     * Récupère tous les dossiers médicaux d'un patient.
     * 
     * @param patientId L'ID du patient dont on veut récupérer les dossiers
     * @return Liste des dossiers médicaux du patient
     */
    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Récupérer les dossiers d'un patient", 
               description = "Retourne tous les dossiers médicaux associés à un patient spécifique.")
    public ResponseEntity<List<MedicalRecord>> getRecordsByPatient(
            @Parameter(description = "ID du patient", required = true)
            @PathVariable String patientId) {
        List<MedicalRecord> records = service.getRecordsByPatientId(patientId);
        return ResponseEntity.ok(records);
    }

    /**
     * Recherche avancée de dossiers médicaux avec plusieurs critères optionnels.
     * 
     * @param patientId L'ID du patient (optionnel)
     * @param providerId L'ID du provider (optionnel)
     * @param from Date de début pour le filtre (optionnel, format ISO)
     * @param to Date de fin pour le filtre (optionnel, format ISO)
     * @param limit Nombre maximum de résultats à retourner (optionnel)
     * @return Liste des dossiers médicaux correspondant aux critères
     */
    @GetMapping("/search")
    @Operation(summary = "Recherche avancée de dossiers médicaux", 
               description = "Recherche des dossiers médicaux avec plusieurs critères optionnels : " +
                           "patient, provider, plage de dates, et limite de résultats.")
    public ResponseEntity<List<MedicalRecord>> searchRecords(
            @Parameter(description = "ID du patient (optionnel)")
            @RequestParam(required = false) String patientId,
            
            @Parameter(description = "ID du provider (optionnel)")
            @RequestParam(required = false) String providerId,
            
            @Parameter(description = "Date de début (optionnel, format: yyyy-MM-ddTHH:mm:ss)")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            
            @Parameter(description = "Date de fin (optionnel, format: yyyy-MM-ddTHH:mm:ss)")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            
            @Parameter(description = "Nombre maximum de résultats (optionnel)")
            @RequestParam(required = false) Integer limit) {
        
        List<MedicalRecord> records = service.searchRecords(patientId, providerId, from, to, limit);
        return ResponseEntity.ok(records);
    }
}
