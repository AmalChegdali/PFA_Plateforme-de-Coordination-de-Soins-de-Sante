package com.patient_service.services;

import com.patient_service.dto.PatientRequestResponseDTO;
import com.patient_service.models.Patient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service pour notifier les patients des mises à jour de leurs demandes.
 * Structure extensible pour supporter différents canaux de notification :
 * - Email ✅ Implémenté
 * - WebSocket (structure prête)
 * - Push notifications (à implémenter)
 * - SMS (à implémenter)
 * 
 * @author Patient-Service Team
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final PatientService patientService;
    private final EmailService emailService;

    /**
     * Notifie un patient d'une réponse à sa demande.
     * 
     * @param response La réponse reçue
     * @param patientId L'ID du patient à notifier
     */
    public void notifyPatient(PatientRequestResponseDTO response, String patientId) {
        if (response == null || patientId == null) {
            log.warn("⚠️ Impossible de notifier : réponse ou patientId manquant");
            return;
        }

        try {
            // Récupérer le patient
            Patient patient = patientService.findById(patientId);
            
            log.info("📧 Notification pour le patient {} - Demande: {} - Statut: {}", 
                    patient.getEmail(), response.getRequestId(), response.getStatus());

            // Envoyer les notifications via différents canaux
            sendEmailNotification(patient, response);
            sendWebSocketNotification(patient, response);
            // TODO: Implémenter push notifications si nécessaire
            // sendPushNotification(patient, response);
            
        } catch (Exception e) {
            log.error("❌ Erreur lors de la notification du patient {} : {}", 
                    patientId, e.getMessage(), e);
        }
    }

    /**
     * Envoie une notification par email.
     * 
     * @param patient Le patient à notifier
     * @param response La réponse à la demande
     */
    private void sendEmailNotification(Patient patient, PatientRequestResponseDTO response) {
        try {
            if (!emailService.isEmailConfigured()) {
                log.warn("⚠️ Service email non configuré - Email non envoyé");
                return;
            }

            // Construire le sujet de l'email
            String subject = "Réponse à votre demande médicale";
            if (response.getSubject() != null && !response.getSubject().isEmpty()) {
                subject = "Réponse à votre demande : " + response.getSubject();
            }

            // Construire le corps de l'email
            String body = buildEmailBody(patient, response);

            // Envoyer l'email
            emailService.sendEmail(patient.getEmail(), subject, body);
            
            log.info("✅ Email de notification envoyé à {} - Sujet: '{}'", 
                    patient.getEmail(), subject);
            
        } catch (Exception e) {
            log.error("❌ Erreur lors de l'envoi de l'email : {}", e.getMessage(), e);
            // Ne pas faire échouer la notification si l'email échoue
        }
    }

    /**
     * Envoie une notification via WebSocket (pour les notifications en temps réel).
     * 
     * @param patient Le patient à notifier
     * @param response La réponse à la demande
     */
    private void sendWebSocketNotification(Patient patient, PatientRequestResponseDTO response) {
        try {
            // TODO: Implémenter WebSocket pour les notifications en temps réel
            // Exemple avec Spring WebSocket ou STOMP
            log.info("🔔 Notification WebSocket préparée pour le patient {} - Demande: {}", 
                    patient.getId(), response.getRequestId());
            
            // Exemple de structure WebSocket :
            // WebSocketMessage message = new WebSocketMessage(
            //     "REQUEST_RESPONSE",
            //     response
            // );
            // webSocketService.sendToUser(patient.getId(), message);
            
        } catch (Exception e) {
            log.error("❌ Erreur lors de l'envoi de la notification WebSocket : {}", e.getMessage(), e);
        }
    }

    /**
     * Construit le corps de l'email de notification.
     * 
     * @param patient Le patient
     * @param response La réponse
     * @return Le corps de l'email formaté
     */
    private String buildEmailBody(Patient patient, PatientRequestResponseDTO response) {
        StringBuilder body = new StringBuilder();
        
        // Salutation personnalisée
        String firstName = patient.getPersonalInfo() != null && 
                          patient.getPersonalInfo().getFirstName() != null 
                          ? patient.getPersonalInfo().getFirstName() 
                          : "Cher patient";
        
        body.append("Bonjour ").append(firstName).append(",\n\n");
        body.append("Vous avez reçu une réponse à votre demande médicale.\n\n");
        
        // Détails de la demande
        body.append("═══════════════════════════════════════════════════════\n");
        body.append("DÉTAILS DE LA DEMANDE\n");
        body.append("═══════════════════════════════════════════════════════\n\n");
        
        body.append("ID de la demande : ").append(response.getRequestId()).append("\n");
        
        if (response.getSubject() != null && !response.getSubject().isEmpty()) {
            body.append("Sujet : ").append(response.getSubject()).append("\n");
        }
        
        if (response.getType() != null && !response.getType().isEmpty()) {
            body.append("Type : ").append(response.getType()).append("\n");
        }
        
        if (response.getPriority() != null && !response.getPriority().isEmpty()) {
            body.append("Priorité : ").append(response.getPriority()).append("\n");
        }
        
        body.append("Statut : ").append(response.getStatus()).append("\n\n");
        
        // Message de réponse
        String message = response.getMessage() != null && !response.getMessage().isEmpty()
                        ? response.getMessage()
                        : (response.getResponseMessage() != null ? response.getResponseMessage() : "Aucun message");
        
        body.append("═══════════════════════════════════════════════════════\n");
        body.append("RÉPONSE\n");
        body.append("═══════════════════════════════════════════════════════\n\n");
        body.append(message).append("\n\n");
        
        // Informations sur le provider
        if (response.getProviderName() != null) {
            body.append("Répondu par : ").append(response.getProviderName());
            if (response.getProviderId() != null) {
                body.append(" (ID: ").append(response.getProviderId()).append(")");
            }
            body.append("\n");
        }
        
        // Date de réponse
        if (response.getResponseDate() != null) {
            body.append("Date de réponse : ").append(response.getResponseDate()).append("\n");
        }
        
        body.append("\n");
        body.append("═══════════════════════════════════════════════════════\n\n");
        
        // Footer
        body.append("Cordialement,\n");
        body.append("L'équipe de la plateforme de soins de santé\n\n");
        body.append("Pour toute question, veuillez contacter notre service client.\n");
        body.append("Cet email est envoyé automatiquement, merci de ne pas y répondre.");
        
        return body.toString();
    }
}

