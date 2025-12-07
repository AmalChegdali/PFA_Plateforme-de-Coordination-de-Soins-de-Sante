package com.request_service.controllers;

import com.request_service.dto.CreateCertificateRequest;
import com.request_service.models.Certificate;
import com.request_service.services.CertificatePdfService;
import com.request_service.services.CertificateService;
import com.request_service.services.DataEnrichmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur REST pour la gestion des certificats médicaux.
 * 
 * @author Request-Service Team
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class CertificateController {

    private final CertificateService certificateService;
    private final CertificatePdfService certificatePdfService;
    private final DataEnrichmentService dataEnrichmentService;

    /**
     * Génère un PDF pour un certificat médical.
     * Accessible aux PATIENTS (leurs propres certificats) et PROVIDERS (tous les certificats).
     */
    @GetMapping("/{id}/print")
    @Tag(name = "📄 Certificate Endpoints", description = "Endpoints pour gérer et imprimer les certificats médicaux")
    @Operation(
            summary = "Générer un PDF de certificat",
            description = "**👤 PATIENT** : Génère un PDF de votre certificat médical.\n\n" +
                         "**👨‍⚕️ PROVIDER** : Génère un PDF de n'importe quel certificat.\n\n" +
                         "Le PDF contient toutes les informations du certificat (patient, provider, contenu, dates). " +
                         "Le fichier PDF peut être téléchargé ou imprimé.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF généré avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié - Token JWT manquant ou invalide"),
            @ApiResponse(responseCode = "403", description = "Accès refusé - Les patients ne peuvent voir que leurs propres certificats"),
            @ApiResponse(responseCode = "404", description = "Certificat non trouvé")
    })
    public ResponseEntity<?> printCertificate(
            @Parameter(description = "ID du certificat", required = true)
            @PathVariable String id,
            @Parameter(hidden = true) Authentication authentication) {
        
        try {
            // Récupérer le certificat
            Certificate certificate = certificateService.getCertificateById(id);
            if (certificate == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Certificat non trouvé");
                error.put("message", "Le certificat avec l'ID " + id + " n'existe pas.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            // Vérifier les permissions
            String role = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(a -> a.getAuthority().replace("ROLE_", ""))
                    .orElse("");
            
            if ("PATIENT".equals(role)) {
                // Vérifier que le patient ne peut voir que ses propres certificats
                if (authentication.getPrincipal() instanceof Jwt jwt) {
                    String jwtPatientId = jwt.getClaimAsString("patientId");
                    if (jwtPatientId == null || !jwtPatientId.equals(certificate.getPatientId())) {
                        log.warn("⚠️ Tentative d'accès non autorisé au certificat {} par le patient {}", 
                                id, jwtPatientId);
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                } else {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }
            
            // Générer le PDF
            byte[] pdfBytes = certificatePdfService.generatePdf(certificate);
            
            // Préparer les headers pour le téléchargement
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", 
                    "certificat_" + certificate.getCertificateId() + ".pdf");
            headers.setContentLength(pdfBytes.length);
            
            log.info("✅ PDF généré et envoyé pour le certificat : {}", certificate.getCertificateId());
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
                    
        } catch (Exception e) {
            log.error("❌ Erreur lors de la génération du PDF : {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur lors de la génération du PDF");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Récupère un certificat par son ID.
     * Accessible aux PATIENTS (leurs propres certificats) et PROVIDERS (tous les certificats).
     */
    @GetMapping("/{id}")
    @Tag(name = "📄 Certificate Endpoints", description = "Endpoints pour gérer et imprimer les certificats médicaux")
    @Operation(
            summary = "Récupérer un certificat par ID",
            description = "**👤 PATIENT** : Récupère votre certificat médical.\n\n" +
                         "**👨‍⚕️ PROVIDER** : Récupère n'importe quel certificat.\n\n" +
                         "Nécessite une authentification JWT.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Certificat récupéré avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié"),
            @ApiResponse(responseCode = "403", description = "Accès refusé"),
            @ApiResponse(responseCode = "404", description = "Certificat non trouvé")
    })
    public ResponseEntity<?> getCertificate(
            @Parameter(description = "ID du certificat", required = true)
            @PathVariable String id,
            @Parameter(hidden = true) Authentication authentication) {
        
        Certificate certificate = certificateService.getCertificateById(id);
        if (certificate == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Vérifier les permissions
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("");
        
        if ("PATIENT".equals(role)) {
            if (authentication.getPrincipal() instanceof Jwt jwt) {
                String jwtPatientId = jwt.getClaimAsString("patientId");
                if (jwtPatientId == null || !jwtPatientId.equals(certificate.getPatientId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        
        return ResponseEntity.ok(certificate);
    }

    /**
     * Crée un nouveau certificat médical.
     * Réservé aux PROVIDERS uniquement.
     * 
     * Le certificat contient :
     * - Nom et prénom du médecin (récupérés depuis le JWT et MongoDB)
     * - Nom et prénom du patient (récupérés depuis MongoDB)
     * - Le cas traité
     * - La date d'émission
     * - La signature
     * - Le numéro du certificat (généré automatiquement si non fourni)
     */
    @PostMapping
    @PreAuthorize("hasRole('PROVIDER')")
    @Tag(name = "📄 Certificate Endpoints", description = "Endpoints pour gérer et imprimer les certificats médicaux")
    @Operation(
            summary = "Créer un certificat médical",
            description = "**👨‍⚕️ PROVIDER UNIQUEMENT**\n\n" +
                         "Crée un nouveau certificat médical pour un patient. " +
                         "Le certificat contient automatiquement :\n" +
                         "- Nom et prénom du médecin (extraits du JWT et MongoDB)\n" +
                         "- Nom et prénom du patient (récupérés depuis MongoDB)\n" +
                         "- Le cas traité (fourni dans la requête)\n" +
                         "- La date d'émission (générée automatiquement)\n" +
                         "- La signature (optionnelle, peut être fournie ou extraite du profil)\n" +
                         "- Le numéro du certificat (généré automatiquement si non fourni)\n\n" +
                         "Nécessite une authentification JWT avec le rôle PROVIDER.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Certificat créé avec succès",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Certificate.class))),
            @ApiResponse(responseCode = "400", description = "Requête invalide - Données manquantes ou invalides"),
            @ApiResponse(responseCode = "401", description = "Non authentifié - Token JWT manquant ou invalide"),
            @ApiResponse(responseCode = "403", description = "Accès refusé - Rôle PROVIDER requis"),
            @ApiResponse(responseCode = "404", description = "Patient non trouvé")
    })
    public ResponseEntity<?> createCertificate(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Données du certificat à créer",
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CreateCertificateRequest.class))
            )
            @Valid @RequestBody CreateCertificateRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        
        try {
            // Extraire les informations du provider depuis le JWT
            if (!(authentication.getPrincipal() instanceof Jwt jwt)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Token JWT invalide");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            String providerId = jwt.getClaimAsString("providerId");
            String providerEmail = jwt.getSubject();
            if (providerId == null) {
                providerId = providerEmail; // Fallback sur l'email si providerId n'existe pas
            }
            
            // Récupérer les informations complètes du provider depuis MongoDB
            DataEnrichmentService.ProviderInfo providerInfo = dataEnrichmentService.getProviderInfo(providerId);
            
            String providerName;
            String providerFirstName;
            String providerLastName;
            String providerProfessionalTitle;
            
            if (providerInfo != null) {
                providerName = providerInfo.getFullName();
                providerFirstName = providerInfo.getFirstName();
                providerLastName = providerInfo.getLastName();
                providerProfessionalTitle = providerInfo.getProfessionalTitle();
            } else {
                // Si on ne trouve pas le provider, essayer avec l'email
                if (!providerId.equals(providerEmail)) {
                    providerInfo = dataEnrichmentService.getProviderInfo(providerEmail);
                    if (providerInfo != null) {
                        providerName = providerInfo.getFullName();
                        providerFirstName = providerInfo.getFirstName();
                        providerLastName = providerInfo.getLastName();
                        providerProfessionalTitle = providerInfo.getProfessionalTitle();
                    } else {
                        // Dernier recours : utiliser l'email mais ne pas l'afficher comme nom
                        log.warn("⚠️ Impossible de récupérer le nom du provider {}, utilisation de valeurs par défaut", providerId);
                        providerName = "Médecin non identifié";
                        providerFirstName = "";
                        providerLastName = "";
                        providerProfessionalTitle = "";
                    }
                } else {
                    log.warn("⚠️ Impossible de récupérer le nom du provider {}, utilisation de valeurs par défaut", providerId);
                    providerName = "Médecin non identifié";
                    providerFirstName = "";
                    providerLastName = "";
                    providerProfessionalTitle = "";
                }
            }
            
            // Récupérer les informations du patient depuis MongoDB
            DataEnrichmentService.PatientInfo patientInfo = dataEnrichmentService.getPatientInfo(request.getPatientId());
            if (patientInfo == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Patient non trouvé");
                error.put("message", "Le patient avec l'ID " + request.getPatientId() + " n'existe pas.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            // Séparer le nom du patient en firstName/lastName
            String[] patientNameParts = splitName(patientInfo.getName());
            String patientFirstName = patientNameParts[0];
            String patientLastName = patientNameParts.length > 1 ? patientNameParts[1] : "";
            
            // Créer le certificat
            Certificate certificate = new Certificate();
            certificate.setPatientId(request.getPatientId());
            certificate.setPatientName(patientInfo.getName());
            certificate.setPatientFirstName(patientFirstName);
            certificate.setPatientLastName(patientLastName);
            certificate.setPatientEmail(patientInfo.getEmail());
            certificate.setProviderId(providerId);
            certificate.setProviderName(providerName);
            certificate.setProviderFirstName(providerFirstName);
            certificate.setProviderLastName(providerLastName);
            certificate.setProviderProfessionalTitle(providerProfessionalTitle);
            certificate.setRequestId(request.getRequestId());
            certificate.setType(request.getType());
            certificate.setTitle(request.getTitle());
            certificate.setContent(request.getContent());
            certificate.setCaseTreated(request.getCaseTreated());
            certificate.setIssueDate(LocalDate.now());
            certificate.setExpiryDate(request.getExpiryDate());
            certificate.setSignature(request.getSignature() != null ? request.getSignature() : providerName);
            certificate.setCertificateNumber(request.getCertificateNumber());
            
            // Créer le certificat via le service
            // Le service va générer automatiquement :
            // - certificateNumber si non fourni
            // - certificateId si non fourni
            // - issueDate si non fourni
            // - status = ACTIVE
            // - createdAt et updatedAt
            Certificate created = certificateService.createCertificate(certificate);
            
            log.info("✅ Certificat créé avec succès : {} (Numéro: {}) pour le patient {} ({}) par le provider {} ({})", 
                    created.getCertificateId(), 
                    created.getCertificateNumber(),
                    request.getPatientId(), 
                    created.getPatientName(),
                    providerId,
                    created.getProviderName());
            
            // Le body de réponse contient toutes les informations :
            // - Informations du patient : id, nom complet, prénom, nom, email
            // - Informations du médecin : id, nom complet, prénom, nom
            // - Informations du certificat : numéro, type, titre, contenu, cas traité
            // - Dates : date d'émission, date d'expiration (si fournie)
            // - Signature
            // - Statut
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
            
        } catch (Exception e) {
            log.error("❌ Erreur lors de la création du certificat : {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur lors de la création du certificat");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Sépare un nom complet en prénom et nom.
     * 
     * @param fullName Le nom complet
     * @return Tableau avec [prénom, nom] ou [nom complet] si un seul mot
     */
    private String[] splitName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return new String[]{"", ""};
        }
        
        String trimmed = fullName.trim();
        String[] parts = trimmed.split("\\s+", 2);
        
        if (parts.length == 1) {
            return new String[]{parts[0], ""};
        }
        
        return parts;
    }
}


