package com.patient_service.services;

import com.patient_service.dto.PatientDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * Service pour publier les patients dans RabbitMQ
 */
@Service
@RequiredArgsConstructor
public class PatientPublisherService {

    private final RabbitTemplate rabbitTemplate;

    private static final String EXCHANGE = "patient-exchange";
    private static final String ROUTING_KEY = "patient.sync.request";

    public void publishPatient(PatientDTO patientDTO) {
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, patientDTO);
        System.out.println("📤 Patient publié dans RabbitMQ : " + patientDTO.getEmail());
    }
}
