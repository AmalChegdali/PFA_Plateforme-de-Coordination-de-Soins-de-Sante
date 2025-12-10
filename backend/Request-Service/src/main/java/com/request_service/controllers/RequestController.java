
package com.request_service.controllers;
import com.request_service.dto.PatientRequestMessageDTO;
import com.request_service.services.PatientRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur REST pour la gestion des demandes de patients.
 * 
 * Ce contrôleur permet aux providers de :
 * - Voir toutes les demandes
 * - Filtrer les demandes par statut
 * - Voir une demande spécifique
 * - Répondre aux demandes
 * - Ajouter des messages aux demandes
 * 
 * @author Request-Service Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth") // ✅ Sécurité JWT requise pour tous les endpoints
public class RequestController {

    private final PatientRequestService patientRequestService;

    /**
     * Récupère les demandes d'un patient spécifique.
     * Endpoint accessible aux patients pour voir leurs propres demandes.
     * Affiche le champ targetProviderId pour indiquer si la demande est destinée à un provider spécifique.
     */
    @GetMapping("/patient/{patientId}")
    @Tag(name = "📋 Patient Endpoints", description = "Endpoints accessibles aux PATIENTS pour gérer leurs demandes")
    @Operation(
            summary = "Récupérer toutes les demandes d'un patient", 
            description = "**👤 PATIENT** : Retourne toutes vos propres demandes. " +
                         "Vous ne pouvez voir que vos propres demandes (vérification automatique via patientId dans le JWT).\n\n" +
                         "**👨‍⚕️ PROVIDER** : Retourne toutes les demandes d'un patient spécifique.\n\n" +
                         "Chaque demande inclut le champ 'targetProviderId' qui indique si la demande est destinée à un provider spécifique. " +
                         "Si targetProviderId est null, la demande est visible par tous les providers.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des demandes récupérée avec succès. " +
                           "Chaque demande contient 'targetProviderId' pour identifier le provider cible.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PatientRequestMessageDTO.class))),
            @ApiResponse(responseCode = "401", description = "Non authentifié - Token JWT manquant ou invalide"),
            @ApiResponse(responseCode = "403", description = "Accès refusé - Les patients ne peuvent voir que leurs propres demandes")
    })
    public ResponseEntity<List<PatientRequestMessageDTO>> getRequestsByPatient(
            @Parameter(description = "ID du patient", required = true)
            @PathVariable String patientId,
            @Parameter(hidden = true) Authentication authentication) {
        
        // Vérifier que si c'est un PATIENT, il ne peut voir que ses propres demandes
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("");
        
        if ("PATIENT".equals(role)) {
            // Extraire le patientId depuis le JWT
            if (authentication.getPrincipal() instanceof Jwt jwt) {
                String jwtPatientId = jwt.getClaimAsString("patientId");
                
                // Vérifier que le patientId dans l'URL correspond au patientId dans le JWT
                if (jwtPatientId == null || !jwtPatientId.equals(patientId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(null);
                }
            } else {
                // Si ce n'est pas un JWT, refuser l'accès
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(null);
            }
        }
        
        List<PatientRequestMessageDTO> requests = patientRequestService.getRequestsByPatientId(patientId);
        return ResponseEntity.ok(requests);
    }

    /**
     * Récupère toutes les demandes.
     * Réservé aux PROVIDER uniquement.
     * 
     * 📌 COMMENT IDENTIFIER UNE DEMANDE DESTINÉE À UN PROVIDER :
     * - Vérifiez le champ "targetProviderId" dans chaque demande
     * - Si targetProviderId = providerId → La demande est destinée à ce provider
     * - Si targetProviderId = null → La demande est visible par tous les providers
     * - Le champ "providerId" indique quel provider a traité la demande (rempli lors de la réponse)
     */
    @GetMapping
    @PreAuthorize("hasRole('PROVIDER')")
    @Tag(name = "👨‍⚕️ Provider Endpoints", description = "Endpoints réservés aux PROVIDERS pour gérer les demandes")
    @Operation(
            summary = "Récupérer toutes les demandes", 
            description = "**👨‍⚕️ PROVIDER UNIQUEMENT**\n\n" +
                         "Retourne la liste complète de toutes les demandes de patients. " +
                         "\n\n" +
                         "📌 COMMENT IDENTIFIER UNE DEMANDE DESTINÉE À UN PROVIDER :\n" +
                         "- Vérifiez le champ 'targetProviderId' dans chaque demande\n" +
                         "- Si targetProviderId = providerId → La demande est destinée à ce provider\n" +
                         "- Si targetProviderId = null → La demande est visible par tous les providers\n" +
                         "- Le champ 'providerId' indique quel provider a traité la demande (rempli lors de la réponse)\n\n" +
                         "Nécessite une authentification JWT avec le rôle PROVIDER.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des demandes récupérée avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PatientRequestMessageDTO.class))),
            @ApiResponse(responseCode = "401", description = "Non authentifié - Token JWT manquant ou invalide"),
            @ApiResponse(responseCode = "403", description = "Accès refusé - Rôle PROVIDER requis")
    })
    public ResponseEntity<List<PatientRequestMessageDTO>> getAllRequests(
            @Parameter(hidden = true) Authentication authentication) {
        List<PatientRequestMessageDTO> requests = patientRequestService.getAllRequests();
        return ResponseEntity.ok(requests);
    }

    /**
     * Récupère les demandes filtrées par statut.
     * Réservé aux PROVIDER uniquement.
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('PROVIDER')")
    @Tag(name = "👨‍⚕️ Provider Endpoints", description = "Endpoints réservés aux PROVIDERS pour gérer les demandes")
    @Operation(
            summary = "Récupérer les demandes par statut", 
            description = "**👨‍⚕️ PROVIDER UNIQUEMENT**\n\n" +
                         "Retourne les demandes filtrées par statut (EN_ATTENTE, TRAITÉ, REFUSÉ, etc.). " +
                         "Chaque demande inclut 'targetProviderId' pour identifier le provider cible. " +
                         "Nécessite une authentification JWT avec le rôle PROVIDER.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des demandes filtrées récupérée avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié - Token JWT manquant ou invalide"),
            @ApiResponse(responseCode = "403", description = "Accès refusé - Rôle PROVIDER requis")
    })
    public ResponseEntity<List<PatientRequestMessageDTO>> getRequestsByStatus(
            @Parameter(description = "Statut de la demande (EN_ATTENTE, TRAITÉ, REFUSÉ, etc.)", required = true)
            @PathVariable String status,
            @Parameter(hidden = true) Authentication authentication) {
        List<PatientRequestMessageDTO> requests = patientRequestService.getRequestsByStatus(status);
        return ResponseEntity.ok(requests);
    }

    /**
     * Récupère toutes les demandes d'un provider spécifique.
     * Réservé aux PROVIDER uniquement.
     * 
     * IMPORTANT : Pour savoir si une demande est destinée à un provider spécifique :
     * - Vérifiez le champ "targetProviderId" dans la réponse
     * - Si targetProviderId = providerId → La demande est destinée à ce provider
     * - Si targetProviderId = null → La demande est visible par tous les providers
     * - Le champ "providerId" indique quel provider a traité la demande (rempli lors de la réponse)
     */
    @GetMapping("/provider/{providerId}")
    @PreAuthorize("hasRole('PROVIDER')")
    @Tag(name = "👨‍⚕️ Provider Endpoints", description = "Endpoints réservés aux PROVIDERS pour gérer les demandes")
    @Operation(
            summary = "Récupérer toutes les demandes d'un provider", 
            description = "**👨‍⚕️ PROVIDER UNIQUEMENT**\n\n" +
                         "Retourne toutes les demandes destinées à un provider spécifique (targetProviderId = providerId) " +
                         "OU traitées par ce provider (providerId = providerId). " +
                         "\n\n" +
                         "📌 COMMENT IDENTIFIER UNE DEMANDE DESTINÉE À UN PROVIDER :\n" +
                         "- Vérifiez le champ 'targetProviderId' dans chaque demande\n" +
                         "- Si targetProviderId = providerId → La demande est destinée à ce provider\n" +
                         "- Si targetProviderId = null → La demande est visible par tous les providers\n" +
                         "- Le champ 'providerId' indique quel provider a traité la demande (rempli lors de la réponse)\n\n" +
                         "Nécessite une authentification JWT avec le rôle PROVIDER.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des demandes récupérée avec succès. " +
                           "Chaque demande contient 'targetProviderId' pour identifier le provider cible.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PatientRequestMessageDTO.class))),
            @ApiResponse(responseCode = "401", description = "Non authentifié - Token JWT manquant ou invalide"),
            @ApiResponse(responseCode = "403", description = "Accès refusé - Rôle PROVIDER requis")
    })
    public ResponseEntity<List<PatientRequestMessageDTO>> getRequestsByProvider(
            @Parameter(description = "ID du provider", required = true)
            @PathVariable String providerId,
            @Parameter(hidden = true) Authentication authentication) {
        List<PatientRequestMessageDTO> requests = patientRequestService.getRequestsByProviderId(providerId);
        return ResponseEntity.ok(requests);
    }

    /**
     * Récupère uniquement les demandes destinées à un provider spécifique (targetProviderId).
     * Réservé aux PROVIDER uniquement.
     * 
     * Cet endpoint retourne uniquement les demandes où targetProviderId = providerId.
     * Il exclut les demandes que le provider a traitées mais qui n'étaient pas initialement destinées à lui.
     */
    @GetMapping("/provider/{providerId}/targeted")
    @PreAuthorize("hasRole('PROVIDER')")
    @Tag(name = "👨‍⚕️ Provider Endpoints", description = "Endpoints réservés aux PROVIDERS pour gérer les demandes")
    @Operation(
            summary = "Récupérer uniquement les demandes destinées à un provider", 
            description = "**👨‍⚕️ PROVIDER UNIQUEMENT**\n\n" +
                         "Retourne uniquement les demandes où targetProviderId = providerId. " +
                         "Cet endpoint exclut les demandes que le provider a traitées mais qui n'étaient pas initialement destinées à lui. " +
                         "\n\n" +
                         "📌 UTILISATION :\n" +
                         "- Utilisez cet endpoint pour voir uniquement les demandes qui vous sont spécifiquement destinées\n" +
                         "- Chaque demande retournée aura targetProviderId = providerId\n" +
                         "- Les demandes avec targetProviderId = null ne seront PAS incluses\n\n" +
                         "Nécessite une authentification JWT avec le rôle PROVIDER.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste des demandes destinées au provider récupérée avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PatientRequestMessageDTO.class))),
            @ApiResponse(responseCode = "401", description = "Non authentifié - Token JWT manquant ou invalide"),
            @ApiResponse(responseCode = "403", description = "Accès refusé - Rôle PROVIDER requis")
    })
    public ResponseEntity<List<PatientRequestMessageDTO>> getRequestsTargetedToProvider(
            @Parameter(description = "ID du provider", required = true)
            @PathVariable String providerId,
            @Parameter(hidden = true) Authentication authentication) {
        List<PatientRequestMessageDTO> requests = patientRequestService.getRequestsTargetedToProvider(providerId);
        return ResponseEntity.ok(requests);
    }

    /**
     * Récupère une demande spécifique par son ID.
     * Réservé aux PROVIDER uniquement.
     */
    @GetMapping("/{requestId}")
    @PreAuthorize("hasRole('PROVIDER')")
    @Tag(name = "👨‍⚕️ Provider Endpoints", description = "Endpoints réservés aux PROVIDERS pour gérer les demandes")
    @Operation(
            summary = "Récupérer une demande par ID", 
            description = "**👨‍⚕️ PROVIDER UNIQUEMENT**\n\n" +
                         "Retourne les détails d'une demande spécifique par son ID. " +
                         "La réponse inclut 'targetProviderId' pour identifier le provider cible. " +
                         "Nécessite une authentification JWT avec le rôle PROVIDER.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Demande trouvée",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PatientRequestMessageDTO.class))),
            @ApiResponse(responseCode = "404", description = "Demande non trouvée"),
            @ApiResponse(responseCode = "401", description = "Non authentifié - Token JWT manquant ou invalide"),
            @ApiResponse(responseCode = "403", description = "Accès refusé - Rôle PROVIDER requis")
    })
    public ResponseEntity<PatientRequestMessageDTO> getRequestById(
            @Parameter(description = "ID de la demande", required = true)
            @PathVariable String requestId,
            @Parameter(hidden = true) Authentication authentication) {
        PatientRequestMessageDTO request = patientRequestService.getRequestById(requestId);
        return request != null 
                ? ResponseEntity.ok(request) 
                : ResponseEntity.notFound().build();
    }

    /**
     * Met à jour le statut d'une demande et envoie la réponse au patient.
     * Réservé aux PROVIDER uniquement.
     */
    @PutMapping("/{requestId}/respond")
    @PreAuthorize("hasRole('PROVIDER')")
    @Tag(name = "👨‍⚕️ Provider Endpoints", description = "Endpoints réservés aux PROVIDERS pour gérer les demandes")
    @Operation(
            summary = "Répondre à une demande", 
            description = "**👨‍⚕️ PROVIDER UNIQUEMENT**\n\n" +
                         "Met à jour le statut d'une demande et envoie la réponse au patient via RabbitMQ. " +
                         "Le patient recevra une notification (email) avec votre réponse. " +
                         "Nécessite une authentification JWT avec le rôle PROVIDER.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Réponse envoyée avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PatientRequestMessageDTO.class))),
            @ApiResponse(responseCode = "400", description = "Requête invalide - Le statut est requis"),
            @ApiResponse(responseCode = "404", description = "Demande non trouvée"),
            @ApiResponse(responseCode = "401", description = "Non authentifié - Token JWT manquant ou invalide"),
            @ApiResponse(responseCode = "403", description = "Accès refusé - Rôle PROVIDER requis")
    })
    public ResponseEntity<?> respondToRequest(
            @Parameter(description = "ID de la demande", required = true)
            @PathVariable String requestId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Corps de la requête contenant le statut et le message de réponse",
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"status\": \"TRAITÉ\", \"responseMessage\": \"Votre demande a été acceptée.\"}"))
            )
            @RequestBody Map<String, String> requestBody,
            @Parameter(hidden = true) Authentication authentication) {
        
        String status = requestBody.get("status");
        String responseMessage = requestBody.get("responseMessage");
        
        if (status == null || status.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Le statut est requis");
            return ResponseEntity.badRequest().body(error);
        }
        
        // Extraire les informations du provider depuis le JWT
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String providerId = jwt.getSubject(); // L'email du provider
        String providerName = jwt.getClaimAsString("fullName");
        if (providerName == null || providerName.isEmpty()) {
            providerName = providerId; // Utiliser l'email si le nom n'est pas disponible
        }
        
        PatientRequestMessageDTO updated = patientRequestService.updateRequestStatus(
                requestId,
                status,
                responseMessage,
                providerId,
                providerName
        );
        
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(updated);
    }

    /**
     * Ajoute un message à une demande.
     * Réservé aux PROVIDER uniquement.
     */
    @PostMapping("/{requestId}/messages")
    @PreAuthorize("hasRole('PROVIDER')")
    @Tag(name = "👨‍⚕️ Provider Endpoints", description = "Endpoints réservés aux PROVIDERS pour gérer les demandes")
    @Operation(
            summary = "Ajouter un message à une demande", 
            description = "**👨‍⚕️ PROVIDER UNIQUEMENT**\n\n" +
                         "Ajoute un message à une demande existante. " +
                         "Ce message sera visible dans l'historique de la demande. " +
                         "Nécessite une authentification JWT avec le rôle PROVIDER.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message ajouté avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PatientRequestMessageDTO.class))),
            @ApiResponse(responseCode = "400", description = "Requête invalide - Le contenu du message est requis"),
            @ApiResponse(responseCode = "404", description = "Demande non trouvée"),
            @ApiResponse(responseCode = "401", description = "Non authentifié - Token JWT manquant ou invalide"),
            @ApiResponse(responseCode = "403", description = "Accès refusé - Rôle PROVIDER requis")
    })
    public ResponseEntity<?> addMessage(
            @Parameter(description = "ID de la demande", required = true)
            @PathVariable String requestId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Corps de la requête contenant le message",
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"content\": \"Message du provider\"}"))
            )
            @RequestBody Map<String, String> requestBody,
            @Parameter(hidden = true) Authentication authentication) {
        
        String content = requestBody.get("content");
        if (content == null || content.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Le contenu du message est requis");
            return ResponseEntity.badRequest().body(error);
        }
        
        // Extraire l'ID du provider depuis le JWT
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String providerId = jwt.getSubject(); // L'email du provider
        
        PatientRequestMessageDTO updated = patientRequestService.addMessage(
                requestId,
                providerId,
                "PROVIDER",
                content
        );
        
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(updated);
    }
}
