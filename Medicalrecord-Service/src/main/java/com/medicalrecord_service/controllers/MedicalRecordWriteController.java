package com.medicalrecord_service.controllers;

import com.medicalrecord_service.models.MedicalRecord;
import com.medicalrecord_service.services.MedicalRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/records")
public class MedicalRecordWriteController {

    @Autowired
    private MedicalRecordService service;

    @PostMapping
    @PreAuthorize("hasRole('PROVIDER')")
    public MedicalRecord createRecord(@RequestBody MedicalRecord record) {
        return service.createRecord(record);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PROVIDER')")
    public MedicalRecord updateRecord(@PathVariable String id, @RequestBody MedicalRecord record) {
        return service.updateRecord(id, record);
    }

    @GetMapping
    public List<MedicalRecord> getAllRecords() {
        return service.getAllRecords();
    }

    @GetMapping("/{id}")
    public Optional<MedicalRecord> getRecordById(@PathVariable String id) {
        return service.getRecordById(id);
    }
}
