package com.medicalrecord_service.controllers;

import com.medicalrecord_service.models.MedicalRecord;
import com.medicalrecord_service.services.MedicalRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Contrôleur REST pour les opérations d'écriture sur les dossiers médicaux.
 * 
 * Ce contrôleur gère :
 * - La création de nouveaux dossiers médicaux (réservé aux providers)
 * - La mise à jour de dossiers existants (réservé aux providers)
 * - La suppression de dossiers (réservé aux providers)
 * - La lecture de tous les dossiers ou d'un dossier spécifique
 * 
 * Toutes les opérations d'écriture nécessitent le rôle PROVIDER.
 * 
 * @author MedicalRecord-Service Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
@Tag(name = "Medical Records - Write Operations", description = "API pour créer, modifier et supprimer des dossiers médicaux")
public class MedicalRecordWriteController {

    // ==================== CHAMPS ====================
    
    /** Service pour la gestion des dossiers médicaux */
    private final MedicalRecordService service;

    // ==================== ENDPOINTS CRUD ====================
    
    /**
     * Crée un nouveau dossier médical.
     * 
     * @param record Le dossier médical à créer (dans le body de la requête)
     * @return Le dossier médical créé avec son ID généré
     */
    @PostMapping
    @PreAuthorize("hasRole('PROVIDER')")
    @Operation(summary = "Créer un nouveau dossier médical", 
               description = "Crée un nouveau dossier médical. Nécessite le rôle PROVIDER.")
    public ResponseEntity<MedicalRecord> createRecord(@RequestBody MedicalRecord record) {
        MedicalRecord created = service.createRecord(record);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Met à jour un dossier médical existant.
     * 
     * @param id L'ID du dossier à mettre à jour
     * @param record Les nouvelles données du dossier (dans le body de la requête)
     * @return Le dossier médical mis à jour
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PROVIDER')")
    @Operation(summary = "Mettre à jour un dossier médical", 
               description = "Met à jour un dossier médical existant. Nécessite le rôle PROVIDER.")
    public ResponseEntity<MedicalRecord> updateRecord(
            @PathVariable String id, 
            @RequestBody MedicalRecord record) {
        MedicalRecord updated = service.updateRecord(id, record);
        return ResponseEntity.ok(updated);
    }

    /**
     * Récupère tous les dossiers médicaux.
     * 
     * @return Liste de tous les dossiers médicaux
     */
    @GetMapping
    @Operation(summary = "Récupérer tous les dossiers médicaux", 
               description = "Retourne la liste complète de tous les dossiers médicaux.")
    public ResponseEntity<List<MedicalRecord>> getAllRecords() {
        List<MedicalRecord> records = service.getAllRecords();
        return ResponseEntity.ok(records);
    }

    /**
     * Récupère un dossier médical par son ID.
     * 
     * @param id L'ID du dossier à récupérer
     * @return Le dossier médical s'il existe, sinon 404
     */
    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un dossier médical par ID", 
               description = "Retourne un dossier médical spécifique par son ID.")
    public ResponseEntity<MedicalRecord> getRecordById(@PathVariable String id) {
        Optional<MedicalRecord> record = service.getRecordById(id);
        return record.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Supprime un dossier médical.
     * 
     * @param id L'ID du dossier à supprimer
     * @return 204 No Content si la suppression réussit
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PROVIDER')")
    @Operation(summary = "Supprimer un dossier médical", 
               description = "Supprime un dossier médical. Nécessite le rôle PROVIDER.")
    public ResponseEntity<Void> deleteRecord(@PathVariable String id) {
        service.deleteRecord(id);
        return ResponseEntity.noContent().build();
    }
}
