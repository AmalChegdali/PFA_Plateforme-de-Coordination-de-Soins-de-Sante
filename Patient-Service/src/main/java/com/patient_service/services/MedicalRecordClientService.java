package com.patient_service.services;

import com.patient_service.dto.MedicalHistoryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service client pour communiquer avec Medicalrecord-Service.
 * 
 * Ce service permet au Patient-Service de récupérer les dossiers médicaux
 * d'un patient depuis Medicalrecord-Service via HTTP REST.
 * 
 * @author Patient-Service Team
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MedicalRecordClientService {

    private final RestTemplate restTemplate;

    @Value("${medicalrecord.service.url:http://localhost:8083}")
    private String medicalRecordServiceUrl;

    /**
     * Récupère tous les dossiers médicaux d'un patient.
     * 
     * @param patientId L'ID du patient
     * @param jwtToken Le token JWT du patient pour l'authentification
     * @return Liste des dossiers médicaux du patient
     */
    public List<MedicalHistoryResponse> getPatientMedicalRecords(String patientId, String jwtToken) {
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl(medicalRecordServiceUrl)
                    .path("/api/records/read/patient/{patientId}")
                    .buildAndExpand(patientId)
                    .toUriString();

            log.info("Récupération des dossiers médicaux pour le patient {} depuis {}", 
                    patientId, url);

            // Créer les headers avec le token JWT
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", "Bearer " + jwtToken);
            org.springframework.http.HttpEntity<?> entity = new org.springframework.http.HttpEntity<>(headers);

            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );

            List<Map<String, Object>> records = response.getBody();
            if (records == null) {
                log.warn("Aucun dossier médical trouvé pour le patient {}", patientId);
                return new ArrayList<>();
            }

            // Convertir les Maps en MedicalHistoryResponse
            List<MedicalHistoryResponse> historyResponses = new ArrayList<>();
            for (Map<String, Object> record : records) {
                MedicalHistoryResponse responseDto = convertToMedicalHistoryResponse(record);
                historyResponses.add(responseDto);
            }

            log.info("✅ {} dossier(s) médical(aux) récupéré(s) pour le patient {}", 
                    historyResponses.size(), patientId);

            return historyResponses;

        } catch (Exception e) {
            log.error("❌ Erreur lors de la récupération des dossiers médicaux pour le patient {} : {}", 
                    patientId, e.getMessage(), e);
            return new ArrayList<>(); // Retourner une liste vide en cas d'erreur
        }
    }

    /**
     * Convertit un Map (réponse JSON) en MedicalHistoryResponse.
     * 
     * @param record Le Map contenant les données du dossier médical
     * @return MedicalHistoryResponse converti
     */
    @SuppressWarnings("unchecked")
    private MedicalHistoryResponse convertToMedicalHistoryResponse(Map<String, Object> record) {
        MedicalHistoryResponse response = new MedicalHistoryResponse();
        
        if (record.get("id") != null) {
            response.setRecordId(record.get("id").toString());
        }
        if (record.get("recordId") != null) {
            response.setRecordId(record.get("recordId").toString());
        }
        if (record.get("patientId") != null) {
            response.setPatientId(record.get("patientId").toString());
        }
        if (record.get("providerId") != null) {
            response.setProviderId(record.get("providerId").toString());
        }
        if (record.get("recordType") != null) {
            response.setRecordType(record.get("recordType").toString());
        }
        if (record.get("visitDate") != null) {
            // Gérer la conversion de date si nécessaire
            response.setVisitDate(parseDateTime(record.get("visitDate")));
        }
        if (record.get("diagnosis") != null) {
            response.setDiagnosis(record.get("diagnosis").toString());
        }
        if (record.get("content") != null && record.get("content") instanceof Map) {
            response.setContent((Map<String, Object>) record.get("content"));
        }
        if (record.get("createdAt") != null) {
            response.setCreatedAt(parseDateTime(record.get("createdAt")));
        }
        if (record.get("updatedAt") != null) {
            response.setUpdatedAt(parseDateTime(record.get("updatedAt")));
        }

        return response;
    }

    /**
     * Parse une date depuis un objet (peut être String ou autre format).
     * 
     * @param dateObj L'objet date à parser
     * @return LocalDateTime ou null si parsing échoue
     */
    private java.time.LocalDateTime parseDateTime(Object dateObj) {
        if (dateObj == null) {
            return null;
        }
        if (dateObj instanceof java.time.LocalDateTime) {
            return (java.time.LocalDateTime) dateObj;
        }
        if (dateObj instanceof String) {
            try {
                return java.time.LocalDateTime.parse((String) dateObj);
            } catch (Exception e) {
                log.warn("Impossible de parser la date : {}", dateObj);
                return null;
            }
        }
        return null;
    }
}

