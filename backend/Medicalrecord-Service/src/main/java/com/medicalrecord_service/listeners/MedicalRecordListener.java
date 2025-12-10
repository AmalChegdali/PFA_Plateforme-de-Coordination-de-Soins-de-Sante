package com.medicalrecord_service.listeners;

import com.medicalrecord_service.config.RabbitConfig;
import com.medicalrecord_service.dto.CreateMedicalRecordRequest;
import com.medicalrecord_service.models.MedicalRecord;
import com.medicalrecord_service.services.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Listener RabbitMQ pour recevoir les demandes de création de dossiers médicaux.
 * 
 * Ce listener écoute les messages provenant du Provider-Service et crée
 * automatiquement les dossiers médicaux dans la base de données.
 * 
 * @author MedicalRecord-Service Team
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MedicalRecordListener {

    private final MedicalRecordService medicalRecordService;

    /**
     * Écoute les demandes de création de dossiers médicaux depuis Provider-Service.
     * Reçoit les messages via RabbitMQ et crée les dossiers médicaux.
     * 
     * @param request La demande de création de dossier médical
     */
    @RabbitListener(queues = RabbitConfig.MEDICAL_RECORD_CREATE_QUEUE)
    public void handleCreateMedicalRecord(CreateMedicalRecordRequest request) {
        log.info("Réception d'une demande de création de dossier médical pour le patient : {}", 
                request.getPatientId());
        
        try {
            // Convertir le DTO en entité MedicalRecord
            MedicalRecord record = convertToMedicalRecord(request);
            
            // Créer le dossier médical
            MedicalRecord created = medicalRecordService.createRecord(record);
            
            log.info("✅ Dossier médical créé avec succès : {} pour le patient {}", 
                    created.getRecordId(), created.getPatientId());
        } catch (Exception e) {
            log.error("❌ Erreur lors de la création du dossier médical pour le patient {} : {}", 
                    request.getPatientId(), e.getMessage(), e);
        }
    }

    /**
     * Convertit le DTO CreateMedicalRecordRequest en entité MedicalRecord.
     * 
     * @param request Le DTO de la demande
     * @return L'entité MedicalRecord
     */
    private MedicalRecord convertToMedicalRecord(CreateMedicalRecordRequest request) {
        MedicalRecord record = new MedicalRecord();
        record.setRecordId(request.getRecordId());
        record.setPatientId(request.getPatientId());
        record.setProviderId(request.getProviderId());
        record.setRecordType(request.getRecordType());
        record.setVisitDate(request.getVisitDate());
        record.setDiagnosis(request.getDiagnosis());
        record.setContent(request.getContent());
        record.setCreatedAt(request.getCreatedAt() != null ? request.getCreatedAt() : 
                java.time.LocalDateTime.now());
        record.setUpdatedAt(request.getUpdatedAt() != null ? request.getUpdatedAt() : 
                java.time.LocalDateTime.now());
        return record;
    }
}

