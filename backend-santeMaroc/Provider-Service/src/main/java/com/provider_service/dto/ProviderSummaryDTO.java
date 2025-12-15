package com.provider_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO pour représenter un résumé d'un provider (utilisé pour la liste publique).
 * Ne contient pas d'informations sensibles comme le mot de passe.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProviderSummaryDTO {
    private String providerID;
    private String email;
    private String fullName;
    private String professionalTitle;
    private String specialty;
    private List<String> subSpecialties;
    private String primaryClinicName;
    private String clinicAddress;
    private String contactNumber;
}

