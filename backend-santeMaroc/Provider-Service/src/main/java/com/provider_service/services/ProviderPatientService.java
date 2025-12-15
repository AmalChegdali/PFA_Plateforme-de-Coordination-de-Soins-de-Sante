package com.provider_service.services;

import com.provider_service.config.RabbitConfig;
import com.provider_service.dto.PatientDTO;
import com.provider_service.dto.PatientStatusUpdateMessageDTO;
import com.provider_service.dto.PatientSyncRequest;
import com.provider_service.enums.AccountStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service pour gérer les patients reçus depuis Patient-Service via RabbitMQ.
 * 
 * Ce service :
 * - Écoute les messages RabbitMQ provenant de Patient-Service
 * - Maintient une liste locale des patients
 * - Fournit des méthodes pour gérer le statut des patients (activer, suspendre)
 * 
 * @author Provider-Service Team
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProviderPatientService {

    // ==================== CONSTANTES ====================
    
    /** Valeur pour récupérer tous les patients sans filtre de statut */
    private static final String STATUS_ALL = "ALL";
    
    /** Valeur par défaut pour fullName si non disponible */
    private static final String DEFAULT_FULL_NAME = "N/A";

    // ==================== CHAMPS ====================
    
    /** Liste en mémoire des patients reçus depuis Patient-Service */
    private final List<PatientDTO> patients = new ArrayList<>();
    
    /** RabbitTemplate pour publier les mises à jour de statut */
    private final RabbitTemplate rabbitTemplate;
    
    /** Flag pour éviter les synchronisations multiples simultanées */
    private volatile boolean syncInProgress = false;
    
    /** Timestamp de la dernière synchronisation */
    private volatile long lastSyncTime = 0;

    // ==================== MÉTHODES PUBLIQUES ====================
    
    /**
     * Ajoute un nouveau patient à la liste.
     * 
     * @param patient Le patient à ajouter
     */
    public void addPatient(PatientDTO patient) {
        patients.add(patient);
        log.debug("Patient ajouté : {}", patient.getEmail());
    }

    /**
     * Récupère la liste des patients filtrés par statut.
     * Si la liste est vide et qu'on demande tous les patients, déclenche automatiquement une synchronisation.
     * 
     * @param providerId L'ID du provider (utilisé pour filtrer les patients assignés)
     * @param status Le statut à filtrer ("ALL" pour tous les patients, ou un AccountStatus)
     * @return Liste des patients correspondant au filtre
     */
    public List<PatientDTO> getPatients(String providerId, String status) {
        // Si la liste est vide et qu'on demande tous les patients, déclencher la synchronisation
        // Éviter les synchronisations multiples simultanées (attendre au moins 5 secondes entre deux)
        long currentTime = System.currentTimeMillis();
        if (patients.isEmpty() && STATUS_ALL.equalsIgnoreCase(status) && 
            !syncInProgress && (currentTime - lastSyncTime > 5000)) {
            log.info("Liste de patients vide, déclenchement automatique de la synchronisation...");
            syncInProgress = true;
            lastSyncTime = currentTime;
            try {
                requestSyncAllPatients(providerId);
            } catch (Exception e) {
                log.error("Erreur lors de la synchronisation automatique : {}", e.getMessage());
                syncInProgress = false;
            }
        }
        
        // Si "ALL" est demandé, retourner tous les patients (avec information d'assignation)
        if (STATUS_ALL.equalsIgnoreCase(status)) {
            return new ArrayList<>(patients);
        }

        // Convertir le statut string en enum
        AccountStatus filterStatus = parseAccountStatus(status);
        
        // Filtrer les patients par statut
        return patients.stream()
                .filter(p -> p.getAccountStatus() == filterStatus)
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les patients assignés à un provider spécifique.
     * 
     * @param providerId L'ID du provider
     * @return Liste des patients assignés à ce provider
     */
    public List<PatientDTO> getAssignedPatients(String providerId) {
        return patients.stream()
                .filter(p -> providerId != null && providerId.equals(p.getAssignedProviderId()))
                .collect(Collectors.toList());
    }
    
    /**
     * Récupère les patients non assignés (sans provider assigné).
     * 
     * @return Liste des patients non assignés
     */
    public List<PatientDTO> getUnassignedPatients() {
        return patients.stream()
                .filter(p -> p.getAssignedProviderId() == null || p.getAssignedProviderId().isEmpty())
                .collect(Collectors.toList());
    }
    
    /**
     * Assigne un patient à un provider.
     * 
     * @param patientId L'ID du patient
     * @param providerId L'ID du provider qui assigne
     * @return Le patient mis à jour, ou null si non trouvé
     */
    public PatientDTO assignPatientToProvider(String patientId, String providerId) {
        PatientDTO patient = getPatientById(null, patientId);
        if (patient != null) {
            String previousProviderId = patient.getAssignedProviderId();
            patient.setAssignedProviderId(providerId);
            log.info("✅ Patient {} assigné au provider {} (précédemment: {})", 
                    patientId, providerId, previousProviderId != null ? previousProviderId : "non assigné");
        } else {
            log.warn("⚠️ Tentative d'assignation d'un patient inexistant : {}", patientId);
        }
        return patient;
    }
    
    /**
     * Désassigne un patient (retire l'assignation à un provider).
     * 
     * @param patientId L'ID du patient
     * @param providerId L'ID du provider qui désassigne (vérification de sécurité)
     * @return Le patient mis à jour, ou null si non trouvé
     */
    public PatientDTO unassignPatientFromProvider(String patientId, String providerId) {
        PatientDTO patient = getPatientById(null, patientId);
        if (patient != null) {
            // Vérifier que le patient est bien assigné à ce provider
            if (providerId.equals(patient.getAssignedProviderId())) {
                patient.setAssignedProviderId(null);
                log.info("✅ Patient {} désassigné du provider {}", patientId, providerId);
            } else {
                log.warn("⚠️ Tentative de désassignation d'un patient non assigné à ce provider : {} (assigné à: {})", 
                        patientId, patient.getAssignedProviderId());
                return null;
            }
        } else {
            log.warn("⚠️ Tentative de désassignation d'un patient inexistant : {}", patientId);
        }
        return patient;
    }

    /**
     * Récupère un patient par son ID.
     * 
     * @param providerId L'ID du provider (non utilisé actuellement)
     * @param patientId L'ID du patient à rechercher
     * @return Le patient trouvé, ou null si non trouvé
     */
    public PatientDTO getPatientById(String providerId, String patientId) {
        return patients.stream()
                .filter(p -> p.getId().equals(patientId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Met à jour le statut d'un patient.
     * Publie également la mise à jour de statut à Patient-Service via RabbitMQ.
     * 
     * @param patientId L'ID du patient à mettre à jour
     * @param status Le nouveau statut à appliquer
     * @param providerId L'ID du provider qui effectue la mise à jour
     */
    public void updatePatientStatus(String patientId, AccountStatus status, String providerId) {
        PatientDTO patient = getPatientById(null, patientId);
        if (patient != null) {
            AccountStatus previousStatus = patient.getAccountStatus();
            patient.setAccountStatus(status);
            log.info("Statut du patient {} mis à jour par le provider {} : {}", patientId, providerId, status);
            
            // Publier la mise à jour de statut à Patient-Service via RabbitMQ
            publishStatusUpdate(patientId, providerId, previousStatus, status, null);
        } else {
            log.warn("Tentative de mise à jour du statut d'un patient inexistant : {}", patientId);
        }
    }

    /**
     * Ajoute un patient s'il n'existe pas, ou met à jour s'il existe déjà.
     * Préserve l'assignation existante lors de la mise à jour.
     * 
     * @param patient Le patient à ajouter ou mettre à jour
     */
    public void addOrUpdatePatient(PatientDTO patient) {
        PatientDTO existing = findPatientById(patient.getId());
        
        if (existing != null) {
            // Préserver l'assignation existante si le patient mis à jour n'a pas d'assignation
            String preservedAssignment = existing.getAssignedProviderId();
            
            // Mise à jour des champs existants
            updatePatientFields(existing, patient);
            
            // Si le patient mis à jour n'a pas d'assignation, préserver l'ancienne
            if (patient.getAssignedProviderId() == null && preservedAssignment != null) {
                existing.setAssignedProviderId(preservedAssignment);
            }
            
            log.debug("Patient mis à jour : {} (assigné à: {})", 
                    patient.getId(), existing.getAssignedProviderId());
        } else {
            // Ajout d'un nouveau patient (non assigné par défaut)
            if (patient.getAssignedProviderId() == null) {
                patient.setAssignedProviderId(null); // Explicitement non assigné
            }
            patients.add(patient);
            log.debug("Nouveau patient ajouté : {} (non assigné)", patient.getId());
        }
    }

    /**
     * Active un patient en changeant son statut à ACTIVE.
     * Publie également la mise à jour de statut à Patient-Service via RabbitMQ.
     * 
     * @param patientId L'ID du patient à activer
     * @param providerId L'ID du provider qui effectue l'activation
     * @return Le patient activé, ou null si non trouvé
     */
    public PatientDTO activatePatient(String patientId, String providerId) {
        PatientDTO patient = getPatientById(null, patientId);
        if (patient != null) {
            AccountStatus previousStatus = patient.getAccountStatus();
            patient.setAccountStatus(AccountStatus.ACTIVE);
            log.info("Patient activé par le provider {} : {}", providerId, patientId);
            
            // Publier la mise à jour de statut à Patient-Service via RabbitMQ
            publishStatusUpdate(patientId, providerId, previousStatus, AccountStatus.ACTIVE, null);
        } else {
            log.warn("Tentative d'activation d'un patient inexistant : {}", patientId);
        }
        return patient;
    }

    /**
     * Suspend un patient avec une raison.
     * Publie également la mise à jour de statut à Patient-Service via RabbitMQ.
     * 
     * @param patientId L'ID du patient à suspendre
     * @param reason La raison de la suspension
     * @param providerId L'ID du provider qui effectue la suspension
     * @return Le patient suspendu, ou null si non trouvé
     */
    public PatientDTO suspendPatient(String patientId, String reason, String providerId) {
        PatientDTO patient = getPatientById(null, patientId);
        if (patient != null) {
            AccountStatus previousStatus = patient.getAccountStatus();
            patient.setAccountStatus(AccountStatus.SUSPENDED);
            patient.setSuspensionReason(reason);
            log.info("Patient suspendu par le provider {} : {} - Raison : {}", providerId, patientId, reason);
            
            // Publier la mise à jour de statut à Patient-Service via RabbitMQ
            publishStatusUpdate(patientId, providerId, previousStatus, AccountStatus.SUSPENDED, reason);
        } else {
            log.warn("Tentative de suspension d'un patient inexistant : {}", patientId);
        }
        return patient;
    }

    // ==================== LISTENER RABBITMQ ====================
    
    /**
     * Écoute les messages RabbitMQ provenant de Patient-Service.
     * Reçoit les nouveaux patients inscrits et les ajoute/met à jour dans la liste locale.
     * 
     * @param patient Le patient reçu depuis Patient-Service
     */
    @RabbitListener(queues = RabbitConfig.PATIENT_SYNC_QUEUE)
    public void receivePatientFromQueue(PatientDTO patient) {
        log.info("Réception d'un patient depuis RabbitMQ : {}", patient.getEmail());
        
        // Construire fullName à partir de firstName et lastName si nécessaire
        buildFullNameIfMissing(patient);
        
        // Ajouter ou mettre à jour le patient
        addOrUpdatePatient(patient);
        
        log.info("✅ Patient traité depuis RabbitMQ : {} ({})", 
                patient.getEmail(), patient.getFullName());
    }

    /**
     * Écoute la réponse de synchronisation des patients depuis Patient-Service.
     * Reçoit la liste complète des patients existants et les ajoute à la liste locale.
     * 
     * @param patientList La liste des patients reçus depuis Patient-Service
     */
    @RabbitListener(queues = RabbitConfig.PATIENT_SYNC_RESPONSE_QUEUE)
    public void receiveSyncResponse(List<PatientDTO> patientList) {
        log.info("Réception de la réponse de synchronisation : {} patients", patientList.size());
        
        int addedCount = 0;
        int updatedCount = 0;
        
        for (PatientDTO patient : patientList) {
            // Construire fullName à partir de firstName et lastName si nécessaire
            buildFullNameIfMissing(patient);
            
            PatientDTO existing = findPatientById(patient.getId());
            if (existing != null) {
                // Mise à jour des champs existants
                updatePatientFields(existing, patient);
                updatedCount++;
            } else {
                // Ajout d'un nouveau patient
                patients.add(patient);
                addedCount++;
            }
        }
        
        log.info("✅ Synchronisation terminée : {} patients ajoutés, {} patients mis à jour", 
                addedCount, updatedCount);
        
        // Réinitialiser le flag de synchronisation
        syncInProgress = false;
    }

    /**
     * Demande la synchronisation de tous les patients existants depuis Patient-Service.
     * Envoie une requête via RabbitMQ pour récupérer tous les patients.
     * 
     * @param providerId L'ID du provider qui demande la synchronisation
     */
    public void requestSyncAllPatients(String providerId) {
        try {
            PatientSyncRequest syncRequest = new PatientSyncRequest(
                    java.util.UUID.randomUUID().toString(),
                    providerId,
                    "ALL"
            );
            
            rabbitTemplate.convertAndSend(
                    RabbitConfig.PATIENT_EXCHANGE,
                    RabbitConfig.PATIENT_SYNC_REQUEST_ROUTING_KEY,
                    syncRequest
            );
            
            log.info("✅ Demande de synchronisation envoyée par le provider {} pour tous les patients", providerId);
        } catch (Exception e) {
            log.error("❌ Erreur lors de l'envoi de la demande de synchronisation : {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la synchronisation des patients", e);
        }
    }

    // ==================== MÉTHODES PRIVÉES ====================
    
    /**
     * Parse un string en AccountStatus enum.
     * Retourne PENDING par défaut si le parsing échoue.
     * 
     * @param status Le string à parser
     * @return L'AccountStatus correspondant, ou PENDING par défaut
     */
    private AccountStatus parseAccountStatus(String status) {
        try {
            return AccountStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Statut invalide '{}', utilisation de PENDING par défaut", status);
            return AccountStatus.PENDING;
        }
    }

    /**
     * Trouve un patient par son ID dans la liste.
     * 
     * @param patientId L'ID du patient à rechercher
     * @return Le patient trouvé, ou null si non trouvé
     */
    private PatientDTO findPatientById(String patientId) {
        return patients.stream()
                .filter(p -> p.getId().equals(patientId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Met à jour les champs d'un patient existant avec les valeurs d'un nouveau patient.
     * Seuls les champs non-null du nouveau patient sont mis à jour.
     * Préserve l'assignation existante si le patient mis à jour n'a pas d'assignation.
     * 
     * @param existing Le patient existant à mettre à jour
     * @param updated Le patient contenant les nouvelles valeurs
     */
    private void updatePatientFields(PatientDTO existing, PatientDTO updated) {
        if (updated.getFullName() != null) existing.setFullName(updated.getFullName());
        if (updated.getFirstName() != null) existing.setFirstName(updated.getFirstName());
        if (updated.getLastName() != null) existing.setLastName(updated.getLastName());
        if (updated.getEmail() != null) existing.setEmail(updated.getEmail());
        if (updated.getPhone() != null) existing.setPhone(updated.getPhone());
        if (updated.getAccountStatus() != null) existing.setAccountStatus(updated.getAccountStatus());
        if (updated.getGender() != null) existing.setGender(updated.getGender());
        if (updated.getAddress() != null) existing.setAddress(updated.getAddress());
        if (updated.getCity() != null) existing.setCity(updated.getCity());
        if (updated.getState() != null) existing.setState(updated.getState());
        if (updated.getZipCode() != null) existing.setZipCode(updated.getZipCode());
        if (updated.getCountry() != null) existing.setCountry(updated.getCountry());
        if (updated.getDateOfBirth() != null) existing.setDateOfBirth(updated.getDateOfBirth());
        // Mettre à jour l'assignation seulement si elle est explicitement fournie
        if (updated.getAssignedProviderId() != null) {
            existing.setAssignedProviderId(updated.getAssignedProviderId());
        }
    }

    /**
     * Construit le fullName à partir de firstName et lastName si fullName est manquant.
     * 
     * @param patient Le patient dont le fullName doit être construit
     */
    private void buildFullNameIfMissing(PatientDTO patient) {
        if (patient.getFullName() == null || patient.getFullName().isEmpty()) {
            StringBuilder fullNameBuilder = new StringBuilder();
            
            if (patient.getFirstName() != null && !patient.getFirstName().isEmpty()) {
                fullNameBuilder.append(patient.getFirstName());
            }
            
            if (patient.getLastName() != null && !patient.getLastName().isEmpty()) {
                if (fullNameBuilder.length() > 0) {
                    fullNameBuilder.append(" ");
                }
                fullNameBuilder.append(patient.getLastName());
            }
            
            String fullName = fullNameBuilder.length() > 0 
                    ? fullNameBuilder.toString() 
                    : DEFAULT_FULL_NAME;
            
            patient.setFullName(fullName);
            log.debug("FullName construit pour patient {} : {}", patient.getId(), fullName);
        }
    }

    /**
     * Publie une mise à jour de statut de patient à Patient-Service via RabbitMQ.
     * 
     * @param patientId L'ID du patient
     * @param providerId L'ID du provider qui effectue le changement
     * @param previousStatus L'ancien statut
     * @param newStatus Le nouveau statut
     * @param reason La raison du changement (optionnel)
     */
    private void publishStatusUpdate(String patientId, String providerId, 
                                    AccountStatus previousStatus, AccountStatus newStatus, String reason) {
        try {
            PatientStatusUpdateMessageDTO message = new PatientStatusUpdateMessageDTO(
                    patientId,
                    providerId,
                    newStatus.name(),
                    previousStatus != null ? previousStatus.name() : "UNKNOWN",
                    reason
            );
            
            rabbitTemplate.convertAndSend(
                    RabbitConfig.PATIENT_EXCHANGE,
                    RabbitConfig.PATIENT_STATUS_ROUTING_KEY,
                    message
            );
            
            log.info("✅ Mise à jour de statut publiée pour le patient {} par le provider {} : {} -> {}", 
                    patientId, providerId, previousStatus, newStatus);
        } catch (Exception e) {
            log.error("❌ Erreur lors de la publication de la mise à jour de statut pour le patient {} : {}", 
                    patientId, e.getMessage(), e);
        }
    }
}
