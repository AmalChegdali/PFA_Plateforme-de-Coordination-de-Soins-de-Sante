/*package com.provider_service.controllers;

import com.patient_service.dto.PatientProfileDTO;
import com.patient_service.dto.RegisterRequest;
import com.patient_service.enums.AccountStatus;
import com.patient_service.models.Patient;
import com.patient_service.services.PatientService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/patients")
@CrossOrigin(origins = "*")
@Tag(name = "Patients", description = "Endpoints for Patients")

public class PatientController {

    @Autowired
    private PatientService patientService;

    // Récupérer tous les patients
    @GetMapping
    public List<PatientProfileDTO> getAllPatients() {
        return patientService.getAllPatients().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Récupérer un patient par ID
    @GetMapping("/{id}")
    public PatientProfileDTO getPatientById(@PathVariable String id) {
        Patient patient = patientService.findById(id);
        return convertToDTO(patient);
    }

    // Filtrer les patients par status
    @GetMapping("/status/{status}")
    public List<PatientProfileDTO> getPatientsByStatus(@PathVariable AccountStatus status) {
        return patientService.getAllPatients().stream()
                .filter(p -> p.getAccountStatus() == status)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    //  Créer un patient via POST
    @PostMapping
    public PatientProfileDTO createPatient(@RequestBody RegisterRequest request) {
        // Ici, on utilise l'email et le mot de passe du DTO RegisterRequest
        Patient patient = patientService.registerPatient(request.getEmail(), request.getPassword(), request);
        return convertToDTO(patient);
    }

    // Méthode de conversion Patient -> DTO
    private PatientProfileDTO convertToDTO(Patient patient) {
        PatientProfileDTO dto = new PatientProfileDTO();
        dto.setId(patient.getId());
        dto.setEmail(patient.getEmail());
        dto.setAccountStatus(patient.getAccountStatus());
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
        dto.setCreatedAt(patient.getCreatedAt());
        dto.setUpdatedAt(patient.getUpdatedAt());
        return dto;
    }
}
*/