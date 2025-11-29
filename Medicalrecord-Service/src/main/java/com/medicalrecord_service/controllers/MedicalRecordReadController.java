package com.medicalrecord_service.controllers;

import com.medicalrecord_service.models.MedicalRecord;
import com.medicalrecord_service.services.MedicalRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/records/read")
public class MedicalRecordReadController {

    @Autowired
    private MedicalRecordService service;

    @GetMapping("/patient/{patientId}")
    public List<MedicalRecord> getRecordsByPatient(@PathVariable String patientId) {
        return service.getRecordsByPatientId(patientId);
    }

    @GetMapping("/search")
    public List<MedicalRecord> searchRecords(
            @RequestParam(required = false) String patientId,
            @RequestParam(required = false) String providerId,
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to,
            @RequestParam(required = false) Integer limit
    ) {
        return service.searchRecords(patientId, providerId, from, to, limit);
    }
}
