package com.provider_service.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.provider_service.config.RabbitConfig;
import com.provider_service.dto.PatientDTO;
import com.provider_service.dto.PatientStatusUpdateMessage;
import com.provider_service.dto.PatientSyncRequest;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProviderPatientService {

	 private final RabbitTemplate rabbitTemplate;
	    
	    // Blocking queue to wait for response
	    private final BlockingQueue<List<PatientDTO>> responseQueue = new ArrayBlockingQueue<>(1);

	    @RabbitListener(queues = RabbitConfig.PATIENT_SYNC_RESPONSE_QUEUE)
	    public void handlePatientResponse(List<PatientDTO> patients) {
	        log.info("Received {} patients from patient service", patients.size());
	        try {
	            responseQueue.clear();
	            responseQueue.offer(patients);
	        } catch (Exception e) {
	            log.error("Error handling patient response: {}", e.getMessage());
	        }
	    }

	    public List<PatientDTO> getPatients(String providerId, String status) {
	        log.info("Getting patients for provider: {} with status: {}", providerId, status);
	        
	        responseQueue.clear();
	        
	        String requestId = UUID.randomUUID().toString();
	        PatientSyncRequest request = new PatientSyncRequest(requestId, providerId, status);
	        
	        rabbitTemplate.convertAndSend(
	                RabbitConfig.PATIENT_EXCHANGE,
	                RabbitConfig.PATIENT_SYNC_ROUTING_KEY,
	                request
	        );
	        
	        log.info("Sent patient sync request, waiting for response...");
	        
	        try {
	            List<PatientDTO> allPatients = responseQueue.poll(3, TimeUnit.SECONDS);
	            
	            if (allPatients == null) {
	                log.warn("No response received within timeout");
	                return List.of();
	            }
	            
	            log.info("Retrieved {} patients", allPatients.size());
	            
	            if ("ALL".equalsIgnoreCase(status)) {
	                return allPatients;
	            }
	            
	            return allPatients.stream()
	                    .filter(p -> status.equalsIgnoreCase(p.getAccountStatus()))
	                    .toList();
	                    
	        } catch (InterruptedException e) {
	            Thread.currentThread().interrupt();
	            log.error("Interrupted while waiting for patient data");
	            return List.of();
	        }
	    }

	    public void activatePatient(String patientId, String providerId, String reason) {
	        log.info("Provider {} activating patient {}", providerId, patientId);
	        
	        PatientStatusUpdateMessage message = new PatientStatusUpdateMessage(
	                patientId, providerId, "ACTIVE", "PENDING");
	        message.setReason(reason);
	        
	        rabbitTemplate.convertAndSend(
	                RabbitConfig.PATIENT_EXCHANGE,
	                RabbitConfig.PATIENT_STATUS_ROUTING_KEY,
	                message
	        );
	        
	        log.info("Sent activation request for patient {}", patientId);
	    }

	    public void deactivatePatient(String patientId, String providerId, String reason) {
	        log.info("Provider {} deactivating patient {}", providerId, patientId);
	        
	        PatientStatusUpdateMessage message = new PatientStatusUpdateMessage(
	                patientId, providerId, "INACTIVE", "ACTIVE");
	        message.setReason(reason);
	        
	        rabbitTemplate.convertAndSend(
	                RabbitConfig.PATIENT_EXCHANGE,
	                RabbitConfig.PATIENT_STATUS_ROUTING_KEY,
	                message
	        );
	        
	        log.info("Sent deactivation request for patient {}", patientId);
	    }
    
}