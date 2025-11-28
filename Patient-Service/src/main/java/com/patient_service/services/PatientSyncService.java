package com.patient_service.services;

import com.patient_service.enums.AccountStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.patient_service.config.RabbitConfig;
import com.patient_service.dto.PatientDTO;
import com.patient_service.dto.PatientSyncRequest;
import com.patient_service.models.Patient;
import com.patient_service.repository.PatientRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientSyncService {

    private final PatientRepository patientRepository;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitConfig.PATIENT_SYNC_QUEUE)
    public void handleSyncRequest(PatientSyncRequest request) {
        log.info("Received sync request from provider: {} for status: {}", 
                request.getProviderId(), request.getStatus());
        
        try {
            List<Patient> patients = getPatientsByStatus(request.getStatus());
            List<PatientDTO> patientDTOs = patients.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            
            // Send to proper exchange and routing key
            rabbitTemplate.convertAndSend(
                    RabbitConfig.PATIENT_EXCHANGE,
                    RabbitConfig.PATIENT_SYNC_RESPONSE_ROUTING_KEY,
                    patientDTOs
            );
            
            log.info("Sent {} patients to response queue", patientDTOs.size());
            
        } catch (Exception e) {
            log.error("Error processing sync request: {}", e.getMessage(), e);
        }
    }

    private List<Patient> getPatientsByStatus(String status) {
        if ("ALL".equalsIgnoreCase(status)) {
            return patientRepository.findAll();
        }
        
        try {
            AccountStatus accountStatus = AccountStatus.valueOf(status.toUpperCase());
            return patientRepository.findByAccountStatus(accountStatus);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid status requested: {}, returning all patients", status);
            return patientRepository.findAll();
        }
    }

    private PatientDTO convertToDTO(Patient patient) {
        PatientDTO dto = new PatientDTO();
        dto.setId(patient.getId());
        dto.setEmail(patient.getEmail());
        dto.setFirstName(patient.getFirstName());
        dto.setLastName(patient.getLastName());
        dto.setPhone(patient.getPhone());
        dto.setDateOfBirth(patient.getDateOfBirth());
        dto.setGender(patient.getGender());
        dto.setAddress(patient.getAddress());
        dto.setCity(patient.getCity());
        dto.setState(patient.getState());
        dto.setZipCode(patient.getZipCode());
        dto.setCountry(patient.getCountry());
        dto.setAccountStatus(patient.getAccountStatus().name());
        dto.setCreatedAt(patient.getCreatedAt());
        dto.setUpdatedAt(patient.getUpdatedAt());
        return dto;
    }
}
