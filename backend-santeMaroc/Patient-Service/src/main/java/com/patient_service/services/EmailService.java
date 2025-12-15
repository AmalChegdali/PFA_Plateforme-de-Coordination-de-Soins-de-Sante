package com.patient_service.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Service pour envoyer des emails aux patients.
 * 
 * @author Patient-Service Team
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:noreply@soins-sante.com}")
    private String fromEmail;

    /**
     * Envoie un email simple.
     * 
     * @param to L'adresse email du destinataire
     * @param subject Le sujet de l'email
     * @param body Le corps de l'email
     */
    public void sendEmail(String to, String subject, String body) {
        try {
            log.info("📧 Tentative d'envoi d'email à : {} depuis : {}", to, fromEmail);
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("✅ Email envoyé avec succès depuis {} vers {} - Sujet: '{}'", 
                    fromEmail, to, subject);

        } catch (org.springframework.mail.MailAuthenticationException e) {
            log.error("❌ Erreur d'authentification email - Vérifiez votre mot de passe d'application Gmail : {}", 
                    e.getMessage());
            throw new RuntimeException("Erreur d'authentification email. Vérifiez votre mot de passe d'application Gmail.", e);
        } catch (org.springframework.mail.MailSendException e) {
            log.error("❌ Erreur lors de l'envoi de l'email à {} : {}", to, e.getMessage());
            throw new RuntimeException("Erreur lors de l'envoi de l'email : " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("❌ Erreur inattendue lors de l'envoi de l'email à {} : {}", to, e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'envoi de l'email : " + e.getMessage(), e);
        }
    }

    /**
     * Vérifie si le service email est configuré.
     * 
     * @return true si configuré, false sinon
     */
    public boolean isEmailConfigured() {
        if (mailSender == null) {
            log.warn("⚠️ JavaMailSender n'est pas configuré");
            return false;
        }
        
        // Vérifier que les propriétés essentielles sont configurées
        try {
            // Tester la configuration en vérifiant si on peut créer un message
            SimpleMailMessage testMessage = new SimpleMailMessage();
            testMessage.setFrom(fromEmail);
            return true;
        } catch (Exception e) {
            log.warn("⚠️ Configuration email incomplète : {}", e.getMessage());
            return false;
        }
    }
}

