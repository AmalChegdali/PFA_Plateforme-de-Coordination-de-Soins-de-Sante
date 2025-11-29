package com.medicalrecord_service.services;

import com.medicalrecord_service.models.MedicalRecord;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class MedicalRecordService {

    private final Map<String, MedicalRecord> database = new HashMap<>();

    public MedicalRecord createRecord(MedicalRecord record) {
        String id = UUID.randomUUID().toString();
        record.setRecordId(id);
        record.setVisitDate(LocalDateTime.now());
        database.put(id, record);
        return record;
    }

    public MedicalRecord updateRecord(String id, MedicalRecord record) {
        if (!database.containsKey(id)) {
            throw new RuntimeException("Record not found");
        }
        MedicalRecord existing = database.get(id);
        existing.setDiagnosis(record.getDiagnosis());
        existing.setContent(record.getContent());
        existing.setVisitDate(LocalDateTime.now());
        return existing;
    }

    public Optional<MedicalRecord> getRecordById(String id) {
        return Optional.ofNullable(database.get(id));
    }

    public List<MedicalRecord> getAllRecords() {
        return new ArrayList<>(database.values());
    }

    public List<MedicalRecord> getRecordsByPatientId(String patientId) {
        List<MedicalRecord> result = new ArrayList<>();
        for (MedicalRecord r : database.values()) {
            if (r.getPatientId().equals(patientId)) {
                result.add(r);
            }
        }
        return result;
    }

    public List<MedicalRecord> searchRecords(String patientId, String providerId,
                                             LocalDateTime from, LocalDateTime to, Integer limit) {
        List<MedicalRecord> result = new ArrayList<>();
        for (MedicalRecord r : database.values()) {
            if ((patientId == null || r.getPatientId().equals(patientId)) &&
                    (providerId == null || r.getProviderId().equals(providerId)) &&
                    (from == null || !r.getVisitDate().isBefore(from)) &&
                    (to == null || !r.getVisitDate().isAfter(to))) {
                result.add(r);
            }
        }
        if (limit != null && result.size() > limit) {
            return result.subList(0, limit);
        }
        return result;
    }
}
