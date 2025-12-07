package com.request_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO pour la création d'un certificat médical.
 * 
 * @author Request-Service Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requête pour créer un certificat médical")
public class CreateCertificateRequest {

    @NotBlank(message = "L'ID du patient est requis")
    @Schema(description = "ID du patient", example = "507f1f77bcf86cd799439011", requiredMode = Schema.RequiredMode.REQUIRED)
    private String patientId;

    @Schema(description = "ID de la demande associée (optionnel)", example = "507f1f77bcf86cd799439012")
    private String requestId;

    @NotBlank(message = "Le type de certificat est requis")
    @Schema(description = "Type de certificat", example = "MEDICAL", requiredMode = Schema.RequiredMode.REQUIRED, 
            allowableValues = {"MEDICAL", "WORK", "SPORT", "SCHOOL", "OTHER"})
    private String type;

    @NotBlank(message = "Le titre du certificat est requis")
    @Schema(description = "Titre du certificat", example = "Certificat médical", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @NotBlank(message = "Le cas traité est requis")
    @Schema(description = "Le cas traité (diagnostic, raison du certificat)", 
            example = "Grippe avec arrêt de travail de 3 jours", requiredMode = Schema.RequiredMode.REQUIRED)
    private String caseTreated;

    @Schema(description = "Contenu détaillé du certificat", 
            example = "Je soussigné certifie que le patient a été examiné et nécessite un arrêt de travail.")
    private String content;

    @Schema(description = "Date d'expiration du certificat (optionnel)", example = "2025-12-31")
    private LocalDate expiryDate;

    @Schema(description = "Signature du médecin (texte ou référence)", example = "Dr. Jean Dupont")
    private String signature;

    @Schema(description = "Numéro du certificat (si non fourni, sera généré automatiquement)", 
            example = "CERT-2025-001")
    private String certificateNumber;
}

