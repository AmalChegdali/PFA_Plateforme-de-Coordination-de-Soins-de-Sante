package com.patient_service.services;

import com.patient_service.dto.PatientRequestResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service pour gérer les réponses aux demandes reçues depuis Request-Service.
 * Stocke les réponses en mémoire pour un accès rapide.
 * 
 * @author Patient-Service Team
 * @version 1.0
 */
@Slf4j
@Service
public class RequestResponseService {

    // Cache en mémoire pour stocker les réponses récentes
    // Clé: requestId, Valeur: PatientRequestResponseDTO
    private final Map<String, PatientRequestResponseDTO> responseCache = new ConcurrentHashMap<>();

    /**
     * Enregistre une réponse à une demande.
     * 
     * @param response La réponse reçue depuis Request-Service
     */
    public void saveResponse(PatientRequestResponseDTO response) {
        if (response == null || response.getRequestId() == null) {
            log.warn("⚠️ Tentative d'enregistrer une réponse invalide");
            return;
        }

        // Mettre à jour le timestamp si disponible
        if (response.getUpdatedAt() == null) {
            response.setUpdatedAt(LocalDateTime.now());
        }

        responseCache.put(response.getRequestId(), response);
        log.info("✅ Réponse enregistrée pour la demande: {} - Statut: {}", 
                response.getRequestId(), response.getStatus());
    }

    /**
     * Récupère une réponse par son requestId.
     * 
     * @param requestId L'ID de la demande
     * @return La réponse ou null si non trouvée
     */
    public PatientRequestResponseDTO getResponse(String requestId) {
        return responseCache.get(requestId);
    }

    /**
     * Vérifie si une réponse existe pour une demande.
     * 
     * @param requestId L'ID de la demande
     * @return true si une réponse existe
     */
    public boolean hasResponse(String requestId) {
        return responseCache.containsKey(requestId);
    }

    /**
     * Supprime une réponse du cache (pour libérer la mémoire).
     * 
     * @param requestId L'ID de la demande
     */
    public void removeResponse(String requestId) {
        responseCache.remove(requestId);
        log.debug("Réponse supprimée du cache pour la demande: {}", requestId);
    }

    /**
     * Récupère toutes les réponses en cache.
     * 
     * @return Map de toutes les réponses
     */
    public Map<String, PatientRequestResponseDTO> getAllResponses() {
        return new ConcurrentHashMap<>(responseCache);
    }

    /**
     * Récupère toutes les notifications (réponses) d'un patient spécifique.
     * 
     * @param patientId L'ID du patient
     * @return Liste des notifications du patient, triées par date de mise à jour (plus récentes en premier)
     */
    public java.util.List<PatientRequestResponseDTO> getPatientNotifications(String patientId) {
        return responseCache.values().stream()
                .filter(response -> patientId != null && patientId.equals(response.getPatientId()))
                .sorted((r1, r2) -> {
                    // Trier par date de mise à jour (plus récentes en premier)
                    if (r1.getUpdatedAt() == null && r2.getUpdatedAt() == null) return 0;
                    if (r1.getUpdatedAt() == null) return 1;
                    if (r2.getUpdatedAt() == null) return -1;
                    return r2.getUpdatedAt().compareTo(r1.getUpdatedAt());
                })
                .collect(java.util.stream.Collectors.toList());
    }
}

