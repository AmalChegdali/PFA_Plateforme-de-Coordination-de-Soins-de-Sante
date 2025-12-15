package com.medicalrecord_service.services;

import com.medicalrecord_service.models.MedicalRecord;
import com.medicalrecord_service.repository.MedicalRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service pour gérer les dossiers médicaux.
 * 
 * Ce service fournit les opérations CRUD pour les dossiers médicaux :
 * - Création, mise à jour, suppression de dossiers
 * - Recherche par patient, provider, ou dates
 * - Récupération du dernier dossier d'un patient
 * 
 * Les données sont persistées dans MongoDB via le repository.
 * 
 * @author MedicalRecord-Service Team
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MedicalRecordService {

    // ==================== CHAMPS ====================
    
    /** Repository MongoDB pour les opérations de persistance */
    private final MedicalRecordRepository repository;

    // ==================== MÉTHODES CRUD ====================
    
    /**
     * Crée un nouveau dossier médical.
     * Génère automatiquement un ID si non fourni et initialise les dates.
     * 
     * @param record Le dossier médical à créer
     * @return Le dossier médical créé avec son ID
     */
    public MedicalRecord createRecord(MedicalRecord record) {
        log.debug("Création d'un nouveau dossier médical pour le patient : {}", record.getPatientId());
        
        // Générer un ID si non fourni
        if (record.getRecordId() == null || record.getRecordId().isEmpty()) {
            record.setRecordId(UUID.randomUUID().toString());
            log.debug("ID généré pour le nouveau dossier : {}", record.getRecordId());
        }
        
        // Initialiser la date de visite si non fournie
        if (record.getVisitDate() == null) {
            record.setVisitDate(LocalDateTime.now());
        }
        
        // Initialiser les timestamps
        LocalDateTime now = LocalDateTime.now();
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        
        MedicalRecord saved = repository.save(record);
        log.info("Dossier médical créé avec succès : {} pour le patient {}", 
                saved.getRecordId(), saved.getPatientId());
        
        return saved;
    }

    /**
     * Met à jour un dossier médical existant.
     * 
     * @param id L'ID du dossier à mettre à jour
     * @param record Les nouvelles données du dossier
     * @return Le dossier mis à jour
     * @throws RuntimeException si le dossier n'existe pas
     */
    public MedicalRecord updateRecord(String id, MedicalRecord record) {
        log.debug("Mise à jour du dossier médical : {}", id);
        
        Optional<MedicalRecord> existingOpt = repository.findById(id);
        if (existingOpt.isEmpty()) {
            log.error("Tentative de mise à jour d'un dossier inexistant : {}", id);
            throw new RuntimeException("Record not found with id: " + id);
        }
        
        MedicalRecord existing = existingOpt.get();
        
        // Mettre à jour uniquement les champs modifiables
        existing.setDiagnosis(record.getDiagnosis());
        existing.setContent(record.getContent());
        existing.setRecordType(record.getRecordType());
        existing.setUpdatedAt(LocalDateTime.now());
        
        MedicalRecord updated = repository.save(existing);
        log.info("Dossier médical mis à jour avec succès : {}", id);
        
        return updated;
    }

    /**
     * Récupère un dossier médical par son ID.
     * 
     * @param id L'ID du dossier à récupérer
     * @return Optional contenant le dossier s'il existe
     */
    public Optional<MedicalRecord> getRecordById(String id) {
        log.debug("Récupération du dossier médical : {}", id);
        return repository.findById(id);
    }

    /**
     * Récupère tous les dossiers médicaux.
     * 
     * @return Liste de tous les dossiers médicaux
     */
    public List<MedicalRecord> getAllRecords() {
        log.debug("Récupération de tous les dossiers médicaux");
        return repository.findAll();
    }

    /**
     * Récupère tous les dossiers médicaux d'un patient.
     * 
     * @param patientId L'ID du patient
     * @return Liste des dossiers médicaux du patient
     */
    public List<MedicalRecord> getRecordsByPatientId(String patientId) {
        log.debug("Récupération des dossiers médicaux pour le patient : {}", patientId);
        return repository.findByPatientId(patientId);
    }

    /**
     * Récupère le dernier dossier médical d'un patient (le plus récent).
     * 
     * @param patientId L'ID du patient
     * @return Optional contenant le dernier dossier s'il existe
     */
    public Optional<MedicalRecord> getLatestRecordByPatientId(String patientId) {
        log.debug("Récupération du dernier dossier médical pour le patient : {}", patientId);
        return repository.findFirstByPatientIdOrderByVisitDateDesc(patientId);
    }

    /**
     * Recherche des dossiers médicaux avec des critères multiples.
     * 
     * @param patientId L'ID du patient (optionnel)
     * @param providerId L'ID du provider (optionnel)
     * @param from Date de début pour le filtre (optionnel)
     * @param to Date de fin pour le filtre (optionnel)
     * @param limit Nombre maximum de résultats (optionnel)
     * @return Liste des dossiers correspondant aux critères
     */
    public List<MedicalRecord> searchRecords(String patientId, String providerId,
                                             LocalDateTime from, LocalDateTime to, Integer limit) {
        log.debug("Recherche de dossiers médicaux - Patient: {}, Provider: {}, From: {}, To: {}, Limit: {}", 
                patientId, providerId, from, to, limit);
        
        // Étape 1 : Récupérer les dossiers selon les critères patient/provider
        List<MedicalRecord> result = fetchRecordsByCriteria(patientId, providerId);
        
        // Étape 2 : Filtrer par date si nécessaire
        result = filterByDateRange(result, from, to);
        
        // Étape 3 : Limiter le nombre de résultats si nécessaire
        result = applyLimit(result, limit);
        
        log.info("Recherche terminée : {} dossier(s) trouvé(s)", result.size());
        
        return result;
    }

    /**
     * Supprime un dossier médical.
     * 
     * @param id L'ID du dossier à supprimer
     */
    public void deleteRecord(String id) {
        log.debug("Suppression du dossier médical : {}", id);
        repository.deleteById(id);
        log.info("Dossier médical supprimé avec succès : {}", id);
    }

    // ==================== MÉTHODES PRIVÉES ====================
    
    /**
     * Récupère les dossiers selon les critères patient/provider.
     * 
     * @param patientId L'ID du patient (optionnel)
     * @param providerId L'ID du provider (optionnel)
     * @return Liste des dossiers correspondant aux critères
     */
    private List<MedicalRecord> fetchRecordsByCriteria(String patientId, String providerId) {
        if (patientId != null && providerId != null) {
            // Recherche par patient ET provider
            return repository.findByPatientIdAndProviderId(patientId, providerId);
        } else if (patientId != null) {
            // Recherche par patient uniquement
            return repository.findByPatientId(patientId);
        } else if (providerId != null) {
            // Recherche par provider uniquement
            return repository.findByProviderId(providerId);
        } else {
            // Aucun critère : retourner tous les dossiers
            return repository.findAll();
        }
    }

    /**
     * Filtre les dossiers par plage de dates.
     * 
     * @param records La liste de dossiers à filtrer
     * @param from Date de début (optionnel)
     * @param to Date de fin (optionnel)
     * @return Liste filtrée des dossiers
     */
    private List<MedicalRecord> filterByDateRange(List<MedicalRecord> records, 
                                                   LocalDateTime from, LocalDateTime to) {
        if (from == null && to == null) {
            // Pas de filtre de date
            return records;
        }
        
        return records.stream()
                .filter(record -> {
                    LocalDateTime visitDate = record.getVisitDate();
                    boolean afterFrom = from == null || !visitDate.isBefore(from);
                    boolean beforeTo = to == null || !visitDate.isAfter(to);
                    return afterFrom && beforeTo;
                })
                .toList();
    }

    /**
     * Limite le nombre de résultats retournés.
     * 
     * @param records La liste de dossiers à limiter
     * @param limit Le nombre maximum de résultats (optionnel)
     * @return Liste limitée des dossiers
     */
    private List<MedicalRecord> applyLimit(List<MedicalRecord> records, Integer limit) {
        if (limit == null || limit <= 0 || records.size() <= limit) {
            return records;
        }
        
        return records.subList(0, limit);
    }
}
