package com.patient_service.controllers;

import com.patient_service.dto.RequestMessageDTO;
import com.patient_service.dto.PatientRequestDTO;
import com.patient_service.dto.PatientRequestResponseDTO;
import com.patient_service.enums.AccountStatus;
import com.patient_service.models.Patient;
import com.patient_service.services.PatientRequestService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/requests")
@CrossOrigin(origins = "*")
@Tag(name = "Requests & Messaging", description = "Gestion des demandes patient & RabbitMQ")
public class RequestController {

    @Autowired
    private PatientRequestService requestService;

    // ✅ 1. POST /api/requests
    @PostMapping(produces = "application/json", consumes = "application/json")
    @Operation(summary = "Soumettre une demande", 
               description = "Soumet une demande. Nécessite un compte ACTIVE.")
    public ResponseEntity<?> submitRequest(
            @Valid @RequestBody PatientRequestDTO requestDTO,
            Authentication authentication
    ) {
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
        
        // Si le compte est actif, traiter la demande
        PatientRequestResponseDTO response =
                requestService.submitRequest(requestDTO, authentication);

        return ResponseEntity.accepted()
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(response);
    }

    // ✅ 2. GET /api/requests
    @GetMapping(produces = "application/json")
    @Operation(summary = "Lister l'historique des demandes",
               description = "Récupère l'historique des demandes. Nécessite un compte ACTIVE.")
    public ResponseEntity<?> getRequests(
            Authentication authentication
    ) {
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
        
        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(requestService.getPatientRequests(authentication));
    }

    // ✅ 3. POST /api/requests/{requestId}/message
    @PostMapping("/{requestId}/message")
    @Operation(summary = "Ajouter un message à une demande",
               description = "Ajoute un message à une demande existante. Nécessite un compte ACTIVE.")
    public ResponseEntity<?> addMessage(
            @PathVariable String requestId,
            @RequestBody RequestMessageDTO messageDTO,
            Authentication authentication
    ) {
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
        
        requestService.addMessage(requestId, messageDTO, authentication);
        return ResponseEntity.ok("Message ajouté avec succès");
    }
}
