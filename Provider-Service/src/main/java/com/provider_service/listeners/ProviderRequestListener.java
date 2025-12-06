package com.provider_service.listeners;

import com.provider_service.config.RabbitConfig;
import com.provider_service.dto.PatientDTO;
import com.provider_service.enums.AccountStatus;
import com.provider_service.services.ProviderPatientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Listener RabbitMQ pour recevoir les messages depuis Patient-Service.
 * 
 * Ce listener gère :
 * - Les nouveaux patients (synchronisation)
 * 
 * Note: Les demandes de patients sont maintenant gérées par Request-Service.
 * 
 * @author Provider-Service Team
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProviderRequestListener {

    private final ProviderPatientService providerPatientService;

    /**
     * Écoute les nouveaux patients depuis Patient-Service.
     * 
     * @param patient Le patient reçu
     */
    @RabbitListener(queues = RabbitConfig.PATIENT_SYNC_QUEUE)
    public void handlePatientRequest(PatientDTO patient) {
        // Forcer le status à PENDING
        patient.setAccountStatus(AccountStatus.PENDING);

        providerPatientService.addOrUpdatePatient(patient);

        log.info("✅ Patient reçu via RabbitMQ : {}", patient.getEmail());
    }
}
