package com.request_service.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service pour enrichir les données des demandes en récupérant les informations
 * manquantes directement depuis MongoDB (partagée avec Patient-Service et Provider-Service).
 * 
 * @author Request-Service Team
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataEnrichmentService {

    private final MongoTemplate mongoTemplate;

    /**
     * Récupère les informations d'un patient directement depuis MongoDB.
     * 
     * @param patientId L'ID du patient
     * @return PatientInfo contenant l'email et le nom du patient, ou null si erreur
     */
    public PatientInfo getPatientInfo(String patientId) {
        if (patientId == null || patientId.isEmpty()) {
            return null;
        }
        
        try {
            log.debug("🔍 Récupération des informations patient depuis MongoDB : {}", patientId);
            
            Query query = new Query(Criteria.where("_id").is(patientId));
            Map patient = mongoTemplate.findOne(query, Map.class, "patients");
            
            if (patient != null) {
                String email = extractString(patient, "email");
                
                // Extraire firstName et lastName depuis personalInfo
                String firstName = null;
                String lastName = null;
                if (patient.get("personalInfo") instanceof Map) {
                    Map<String, Object> personalInfo = (Map<String, Object>) patient.get("personalInfo");
                    firstName = extractString(personalInfo, "firstName");
                    lastName = extractString(personalInfo, "lastName");
                }
                
                // Si personalInfo n'existe pas, essayer directement
                if (firstName == null) {
                    firstName = extractString(patient, "firstName");
                }
                if (lastName == null) {
                    lastName = extractString(patient, "lastName");
                }
                
                // Construire le nom complet
                String fullName = null;
                if (firstName != null && lastName != null) {
                    fullName = firstName + " " + lastName;
                } else if (firstName != null) {
                    fullName = firstName;
                } else if (lastName != null) {
                    fullName = lastName;
                }
                
                log.debug("✅ Informations patient récupérées : email={}, name={}", email, fullName);
                return new PatientInfo(email, fullName);
            }
        } catch (Exception e) {
            log.warn("⚠️ Impossible de récupérer les informations patient {} : {}", patientId, e.getMessage());
        }
        
        return null;
    }

    /**
     * Récupère le nom d'un provider directement depuis MongoDB.
     * 
     * @param providerId L'ID du provider
     * @return Le nom du provider ou null si erreur
     */
    public String getProviderName(String providerId) {
        if (providerId == null || providerId.isEmpty()) {
            return null;
        }
        
        try {
            log.debug("🔍 Récupération des informations provider depuis MongoDB : {}", providerId);
            
            Query query = new Query(Criteria.where("providerID").is(providerId));
            Map provider = mongoTemplate.findOne(query, Map.class, "providers");
            
            if (provider != null) {
                String fullName = extractString(provider, "fullName");
                
                // Si fullName n'existe pas, essayer de construire depuis firstName/lastName
                if (fullName == null || fullName.isEmpty()) {
                    String firstName = extractString(provider, "firstName");
                    String lastName = extractString(provider, "lastName");
                    if (firstName != null && lastName != null) {
                        fullName = firstName + " " + lastName;
                    } else if (firstName != null) {
                        fullName = firstName;
                    } else if (lastName != null) {
                        fullName = lastName;
                    }
                }
                
                log.debug("✅ Informations provider récupérées : name={}", fullName);
                return fullName;
            }
        } catch (Exception e) {
            log.warn("⚠️ Impossible de récupérer les informations provider {} : {}", providerId, e.getMessage());
        }
        
        return null;
    }

    /**
     * Extrait une valeur String d'un Map de manière sécurisée.
     */
    private String extractString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * Classe interne pour stocker les informations patient.
     */
    public static class PatientInfo {
        private final String email;
        private final String name;

        public PatientInfo(String email, String name) {
            this.email = email;
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public String getName() {
            return name;
        }
    }
}

