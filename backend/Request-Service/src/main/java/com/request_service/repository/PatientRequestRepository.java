package com.request_service.repository;

import com.request_service.models.PatientRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository MongoDB pour les demandes de patients.
 * 
 * @author Request-Service Team
 * @version 1.0
 */
@Repository
public interface PatientRequestRepository extends MongoRepository<PatientRequest, String> {
    
    /**
     * Recherche toutes les demandes d'un patient.
     * 
     * @param patientId L'ID du patient
     * @return Liste des demandes du patient
     */
    List<PatientRequest> findByPatientId(String patientId);
    
    /**
     * Recherche toutes les demandes traitées par un provider.
     * 
     * @param providerId L'ID du provider
     * @return Liste des demandes du provider
     */
    List<PatientRequest> findByProviderId(String providerId);
    
    /**
     * Recherche une demande par son requestId.
     * 
     * @param requestId L'ID de la demande
     * @return Optional contenant la demande si trouvée
     */
    Optional<PatientRequest> findByRequestId(String requestId);
    
    /**
     * Recherche les demandes filtrées par statut.
     * 
     * @param status Le statut à filtrer
     * @return Liste des demandes correspondant au statut
     */
    List<PatientRequest> findByStatus(String status);
    
    /**
     * Recherche les demandes d'un patient filtrées par statut.
     * 
     * @param patientId L'ID du patient
     * @param status Le statut à filtrer
     * @return Liste des demandes correspondantes
     */
    List<PatientRequest> findByPatientIdAndStatus(String patientId, String status);
    
    /**
     * Recherche les demandes destinées à un provider ou traitées par un provider.
     * 
     * @param targetProviderId L'ID du provider cible
     * @param providerId L'ID du provider qui a traité
     * @return Liste des demandes correspondantes
     */
    List<PatientRequest> findByTargetProviderIdOrProviderId(String targetProviderId, String providerId);
    
    /**
     * Recherche uniquement les demandes destinées à un provider spécifique.
     * 
     * @param targetProviderId L'ID du provider cible
     * @return Liste des demandes destinées à ce provider
     */
    List<PatientRequest> findByTargetProviderId(String targetProviderId);
}
