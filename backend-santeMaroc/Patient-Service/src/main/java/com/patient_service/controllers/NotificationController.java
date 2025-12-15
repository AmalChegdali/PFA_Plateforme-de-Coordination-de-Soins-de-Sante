package com.patient_service.controllers;

import com.patient_service.dto.PatientRequestResponseDTO;
import com.patient_service.enums.AccountStatus;
import com.patient_service.models.Patient;
import com.patient_service.services.RequestResponseService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur pour gérer les notifications des patients.
 * Les notifications sont les réponses aux demandes reçues depuis Request-Service.
 * 
 * @author Patient-Service Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
@Tag(name = "Notifications", description = "Gestion des notifications patient (réponses aux demandes)")
public class NotificationController {

    @Autowired
    private RequestResponseService requestResponseService;

    /**
     * Récupère toutes les notifications du patient connecté.
     * Les notifications sont les réponses aux demandes reçues depuis Request-Service.
     * 
     * @param authentication L'authentification du patient
     * @return Liste des notifications du patient
     */
    @GetMapping(produces = "application/json")
    @Operation(
            summary = "Lister les notifications", 
            description = "Récupère toutes les notifications (réponses aux demandes) du patient connecté. " +
                         "Les notifications sont triées par date (plus récentes en premier). " +
                         "Nécessite un compte ACTIVE."
    )
    public ResponseEntity<?> getNotifications(Authentication authentication) {
        // Vérifier que le patient est authentifié
        Patient patient = (Patient) authentication.getPrincipal();
        
        // Vérifier que le compte est activé
        if (patient.getAccountStatus() != AccountStatus.ACTIVE) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Account not activated");
            errorResponse.put("message", "Votre compte n'est pas encore activé. Veuillez attendre l'approbation du prestataire de santé.");
            errorResponse.put("accountStatus", patient.getAccountStatus().name());
            errorResponse.put("statusCode", HttpStatus.FORBIDDEN.value());
            
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(errorResponse);
        }
        
        // Récupérer les notifications du patient
        List<PatientRequestResponseDTO> notifications = 
                requestResponseService.getPatientNotifications(patient.getId());
        
        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(notifications);
    }

    /**
     * Récupère une notification spécifique par son requestId.
     * 
     * @param requestId L'ID de la demande
     * @param authentication L'authentification du patient
     * @return La notification ou 404 si non trouvée
     */
    @GetMapping("/{requestId}")
    @Operation(
            summary = "Récupérer une notification par ID", 
            description = "Récupère une notification spécifique par l'ID de la demande. " +
                         "Nécessite un compte ACTIVE."
    )
    public ResponseEntity<?> getNotification(
            @PathVariable String requestId,
            Authentication authentication) {
        
        // Vérifier que le patient est authentifié
        Patient patient = (Patient) authentication.getPrincipal();
        
        // Vérifier que le compte est activé
        if (patient.getAccountStatus() != AccountStatus.ACTIVE) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Account not activated");
            errorResponse.put("message", "Votre compte n'est pas encore activé. Veuillez attendre l'approbation du prestataire de santé.");
            errorResponse.put("accountStatus", patient.getAccountStatus().name());
            errorResponse.put("statusCode", HttpStatus.FORBIDDEN.value());
            
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(errorResponse);
        }
        
        // Récupérer la notification
        PatientRequestResponseDTO notification = requestResponseService.getResponse(requestId);
        
        if (notification == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Vérifier que la notification appartient au patient
        if (!patient.getId().equals(notification.getPatientId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Accès refusé - Cette notification ne vous appartient pas"));
        }
        
        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(notification);
    }
}

