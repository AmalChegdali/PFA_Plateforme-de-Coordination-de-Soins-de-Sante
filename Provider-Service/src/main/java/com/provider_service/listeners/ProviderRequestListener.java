package com.provider_service.listeners;

import com.provider_service.dto.PatientDTO;
import com.provider_service.dto.PatientRequestDTO;
import com.provider_service.enums.AccountStatus;
import com.provider_service.services.ProviderPatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
@Component
@RequiredArgsConstructor
public class ProviderRequestListener {

    private final ProviderPatientService providerPatientService;

    @RabbitListener(queues = "patient.requests.queue")
    public void handlePatientRequest(PatientDTO patient) {

        // Forcer le status à PENDING
        patient.setAccountStatus(AccountStatus.PENDING);

        providerPatientService.addOrUpdatePatient(patient);

        System.out.println("✅ Patient reçu via RabbitMQ : " + patient.getEmail());
    }
}
