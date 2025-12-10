package com.medicalrecord_service.repository;

import com.medicalrecord_service.models.MedicalRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository MongoDB pour les dossiers médicaux.
 * 
 * Cette interface étend MongoRepository et fournit des méthodes de recherche
 * personnalisées pour les dossiers médicaux.
 * 
 * Les méthodes de recherche suivent la convention de nommage Spring Data MongoDB :
 * - findByXxx : recherche par champ Xxx
 * - findByXxxAndYyy : recherche par plusieurs champs
 * - findFirstByXxxOrderByYyyDesc : recherche du premier résultat trié
 * 
 * @author MedicalRecord-Service Team
 * @version 1.0
 */
@Repository
public interface MedicalRecordRepository extends MongoRepository<MedicalRecord, String> {
    
    /**
     * Recherche tous les dossiers médicaux d'un patient.
     * 
     * @param patientId L'ID du patient
     * @return Liste des dossiers médicaux du patient
     */
    List<MedicalRecord> findByPatientId(String patientId);
    
    /**
     * Recherche tous les dossiers médicaux créés par un provider.
     * 
     * @param providerId L'ID du provider
     * @return Liste des dossiers médicaux du provider
     */
    List<MedicalRecord> findByProviderId(String providerId);
    
    /**
     * Recherche les dossiers médicaux d'un patient créés par un provider spécifique.
     * 
     * @param patientId L'ID du patient
     * @param providerId L'ID du provider
     * @return Liste des dossiers médicaux correspondants
     */
    List<MedicalRecord> findByPatientIdAndProviderId(String patientId, String providerId);
    
    /**
     * Recherche les dossiers médicaux dans une plage de dates.
     * 
     * @param from Date de début (incluse)
     * @param to Date de fin (incluse)
     * @return Liste des dossiers médicaux dans la plage de dates
     */
    List<MedicalRecord> findByVisitDateBetween(LocalDateTime from, LocalDateTime to);
    
    /**
     * Recherche le dernier dossier médical d'un patient (le plus récent).
     * 
     * @param patientId L'ID du patient
     * @return Optional contenant le dernier dossier s'il existe
     */
    Optional<MedicalRecord> findFirstByPatientIdOrderByVisitDateDesc(String patientId);
}

