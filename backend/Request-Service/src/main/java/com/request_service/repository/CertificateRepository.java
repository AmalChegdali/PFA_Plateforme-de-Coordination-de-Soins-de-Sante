package com.request_service.repository;

import com.request_service.models.Certificate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository pour la gestion des certificats.
 * 
 * @author Request-Service Team
 * @version 1.0
 */
@Repository
public interface CertificateRepository extends MongoRepository<Certificate, String> {
    
    /**
     * Trouve un certificat par son certificateId.
     * 
     * @param certificateId L'ID unique du certificat
     * @return Le certificat trouvé ou Optional.empty()
     */
    Optional<Certificate> findByCertificateId(String certificateId);
    
    /**
     * Trouve tous les certificats d'un patient.
     * 
     * @param patientId L'ID du patient
     * @return Liste des certificats du patient
     */
    java.util.List<Certificate> findByPatientId(String patientId);
    
    /**
     * Trouve tous les certificats émis par un provider.
     * 
     * @param providerId L'ID du provider
     * @return Liste des certificats émis par le provider
     */
    java.util.List<Certificate> findByProviderId(String providerId);
}


