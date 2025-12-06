package com.request_service.controllers;

import com.request_service.models.Certificate;
import com.request_service.services.CertificatePdfService;
import com.request_service.services.CertificateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.util.HashMap;
import java.util.List;
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
}


