package com.patient_service.services;

import com.patient_service.enums.AccountStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.patient_service.config.RabbitConfig;
import com.patient_service.dto.PatientStatusUpdateMessage;
import com.patient_service.models.Patient;
import com.patient_service.repository.PatientRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientStatusService {

    private final PatientRepository patientRepository;

    @RabbitListener(queues = RabbitConfig.PATIENT_STATUS_QUEUE)
    public void handleStatusUpdate(PatientStatusUpdateMessage message) {
        log.info("Received status update for patient: {}", message.getPatientId());
        
        try {
            Optional<Patient> patientOpt = patientRepository.findById(message.getPatientId());
            
            if (patientOpt.isEmpty()) {
                log.error("Patient not found with ID: {}", message.getPatientId());
                return;
            }
            
            Patient patient = patientOpt.get();
            AccountStatus newStatus = AccountStatus.valueOf(message.getNewStatus());
            
            log.info("Updating patient {} status from {} to {}", 
                    patient.getId(), patient.getAccountStatus(), newStatus);
            
            patient.setAccountStatus(newStatus);
            patient.setUpdatedAt(LocalDateTime.now());
            
            patientRepository.save(patient);
            
            log.info("Successfully updated patient {} status to {}", 
                    patient.getId(), newStatus);
            
        } catch (Exception e) {
            log.error("Error processing status update for patient {}: {}", 
                    message.getPatientId(), e.getMessage(), e);
            // You might want to implement a dead letter queue here
        }
    }
}
