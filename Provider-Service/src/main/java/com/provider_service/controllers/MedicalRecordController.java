package com.provider_service.controllers;

import com.provider_service.dto.CreateMedicalRecordRequest;
import com.provider_service.models.Provider;
import com.provider_service.services.MedicalRecordPublisherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Contrôleur REST pour la gestion des dossiers médicaux.
 * 
 * Ce contrôleur permet aux providers de créer des dossiers médicaux
 * en envoyant des demandes au Medicalrecord-Service via RabbitMQ.
 * 
 * @author Provider-Service Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/providers/medical-records")
@RequiredArgsConstructor
@Tag(name = "Medical Records", description = "Endpoints pour créer des dossiers médicaux (réservé aux providers)")
public class MedicalRecordController {

    private final MedicalRecordPublisherService medicalRecordPublisherService;

    /**
     * Crée un nouveau dossier médical.
     * Envoie la demande au Medicalrecord-Service via RabbitMQ.
     * 
     * @param request La demande de création de dossier médical
     * @param authentication L'authentification du provider
     * @return Message de confirmation
     */
    @PostMapping
    @PreAuthorize("hasRole('PROVIDER')")
    @Operation(summary = "Créer un nouveau dossier médical", 
               description = "Crée un nouveau dossier médical en envoyant la demande au Medicalrecord-Service via RabbitMQ. " +
                           "Nécessite le rôle PROVIDER.")
    public ResponseEntity<Map<String, String>> createMedicalRecord(
            @RequestBody CreateMedicalRecordRequest request,
            Authentication authentication) {
        
        // Récupérer le provider authentifié
        Provider provider = (Provider) authentication.getPrincipal();
        
        // S'assurer que le providerId correspond au provider authentifié
        request.setProviderId(provider.getId());
        
        // Initialiser les dates si non fournies
        if (request.getCreatedAt() == null) {
            request.setCreatedAt(LocalDateTime.now());
        }
        if (request.getUpdatedAt() == null) {
            request.setUpdatedAt(LocalDateTime.now());
        }
        
        try {
            // Publier la demande via RabbitMQ
            medicalRecordPublisherService.publishCreateMedicalRecord(request);
            
            return ResponseEntity.ok(Map.of(
                "message", "Demande de création de dossier médical envoyée avec succès",
                "status", "success",
                "patientId", request.getPatientId(),
                "providerId", request.getProviderId()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "message", "Erreur lors de la création du dossier médical : " + e.getMessage(),
                "status", "error"
            ));
        }
    }
}

