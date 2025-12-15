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
            
            // Essayer d'abord avec providerID, puis avec _id
            Query query = new Query(Criteria.where("providerID").is(providerId));
            Map provider = mongoTemplate.findOne(query, Map.class, "providers");
            
            // Si non trouvé avec providerID, essayer avec _id
            if (provider == null) {
                query = new Query(Criteria.where("_id").is(providerId));
                provider = mongoTemplate.findOne(query, Map.class, "providers");
            }
            
            // Si toujours non trouvé, essayer avec email (au cas où providerId serait un email)
            if (provider == null && providerId.contains("@")) {
                query = new Query(Criteria.where("email").is(providerId));
                provider = mongoTemplate.findOne(query, Map.class, "providers");
            }
            
            if (provider != null) {
                String fullName = extractString(provider, "fullName");
                
                // Si fullName n'existe pas ou est vide, essayer de construire depuis firstName/lastName
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
                
                // Si toujours pas de nom, ne pas retourner l'email
                if (fullName == null || fullName.isEmpty()) {
                    log.warn("⚠️ Provider {} trouvé mais aucun nom disponible", providerId);
                    return null;
                }
                
                log.debug("✅ Informations provider récupérées : name={}", fullName);
                return fullName;
            } else {
                log.warn("⚠️ Provider {} non trouvé dans MongoDB", providerId);
            }
        } catch (Exception e) {
            log.warn("⚠️ Impossible de récupérer les informations provider {} : {}", providerId, e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Récupère les informations complètes d'un provider (nom, prénom, nom de famille).
     * 
     * @param providerId L'ID du provider
     * @return ProviderInfo contenant les informations du provider, ou null si erreur
     */
    public ProviderInfo getProviderInfo(String providerId) {
        if (providerId == null || providerId.isEmpty()) {
            return null;
        }
        
        try {
            log.debug("🔍 Récupération des informations complètes provider depuis MongoDB : {}", providerId);
            
            // Essayer d'abord avec providerID, puis avec _id
            Query query = new Query(Criteria.where("providerID").is(providerId));
            Map provider = mongoTemplate.findOne(query, Map.class, "providers");
            
            // Si non trouvé avec providerID, essayer avec _id
            if (provider == null) {
                query = new Query(Criteria.where("_id").is(providerId));
                provider = mongoTemplate.findOne(query, Map.class, "providers");
            }
            
            // Si toujours non trouvé, essayer avec email
            if (provider == null && providerId.contains("@")) {
                query = new Query(Criteria.where("email").is(providerId));
                provider = mongoTemplate.findOne(query, Map.class, "providers");
            }
            
            if (provider != null) {
                String fullName = extractString(provider, "fullName");
                String firstName = extractString(provider, "firstName");
                String lastName = extractString(provider, "lastName");
                
                // Si fullName n'existe pas, construire depuis firstName/lastName
                if ((fullName == null || fullName.isEmpty()) && (firstName != null || lastName != null)) {
                    if (firstName != null && lastName != null) {
                        fullName = firstName + " " + lastName;
                    } else if (firstName != null) {
                        fullName = firstName;
                    } else if (lastName != null) {
                        fullName = lastName;
                    }
                }
                
                // Si firstName/lastName n'existent pas, essayer de les extraire de fullName
                if ((firstName == null || firstName.isEmpty()) && fullName != null) {
                    String[] parts = fullName.split("\\s+", 2);
                    firstName = parts[0];
                    if (parts.length > 1) {
                        lastName = parts[1];
                    }
                }
                
                // Ne pas retourner si on n'a que l'email
                if (fullName == null || fullName.isEmpty() || fullName.contains("@")) {
                    log.warn("⚠️ Provider {} trouvé mais nom invalide : {}", providerId, fullName);
                    return null;
                }
                
                // Récupérer le titre professionnel
                String professionalTitle = extractString(provider, "professionalTitle");
                
                log.debug("✅ Informations complètes provider récupérées : fullName={}, firstName={}, lastName={}, professionalTitle={}", 
                        fullName, firstName, lastName, professionalTitle);
                return new ProviderInfo(fullName, firstName, lastName, professionalTitle);
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
    
    /**
     * Classe interne pour stocker les informations provider.
     */
    public static class ProviderInfo {
        private final String fullName;
        private final String firstName;
        private final String lastName;
        private final String professionalTitle;

        public ProviderInfo(String fullName, String firstName, String lastName, String professionalTitle) {
            this.fullName = fullName;
            this.firstName = firstName != null ? firstName : "";
            this.lastName = lastName != null ? lastName : "";
            this.professionalTitle = professionalTitle != null ? professionalTitle : "";
        }

        public String getFullName() {
            return fullName;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getProfessionalTitle() {
            return professionalTitle;
        }
    }
}

