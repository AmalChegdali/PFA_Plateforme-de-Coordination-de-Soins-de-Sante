package com.patient_service.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import com.patient_service.dto.MedicalHistoryResponse;
import com.patient_service.dto.PatientProfileDTO;
import com.patient_service.dto.ProfileStatusResponse;
import com.patient_service.enums.AccountStatus;
import com.patient_service.models.Patient;
import com.patient_service.services.MedicalRecordClientService;
import com.patient_service.services.PatientService;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patient")
@CrossOrigin(origins = "*")
@Tag(name = "Patient Profile", description = "Patient profile management")
public class PatientProfileController {

    @Autowired
    private PatientService patientService;

    @Autowired
    private MedicalRecordClientService medicalRecordClientService;

    // ✅ PROFILE STATUS
    @GetMapping("/profile-status")
    @Operation(summary = "Get profile status")
    public ResponseEntity<ProfileStatusResponse> getProfileStatus(Authentication authentication) {
        try {
            Patient patient = (Patient) authentication.getPrincipal();
            ProfileStatusResponse status = patientService.getProfileStatus(patient.getId());
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // GET PROFILE
    @GetMapping("/profile")
    @Operation(summary = "Get patient profile")
    public ResponseEntity<PatientProfileDTO> getProfile(Authentication authentication) {
        try {
            Patient patient = (Patient) authentication.getPrincipal();
            patient = patientService.findById(patient.getId());
            return ResponseEntity.ok(convertToProfileDTO(patient));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // UPDATE PROFILE
    @PutMapping("/complete-profile")
    @Operation(summary = "Update patient profile")
    public ResponseEntity<PatientProfileDTO> updateProfile(
            @Valid @RequestBody Patient profileUpdates,
            Authentication authentication
    ) {
        try {
            Patient currentPatient = (Patient) authentication.getPrincipal();
            Patient updatedPatient = patientService.updatePatientProfile(
                    currentPatient.getId(),
                    profileUpdates
            );

            return ResponseEntity.ok(convertToProfileDTO(updatedPatient));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    //  GET MEDICAL HISTORY
    @GetMapping("/medical-history")
    @Operation(summary = "Get patient medical history", 
               description = "Récupère tous les dossiers médicaux du patient authentifié. " +
                           "Nécessite un compte ACTIVE.")
    public ResponseEntity<?> getMedicalHistory(
            Authentication authentication,
            HttpServletRequest request) {
        try {
            Patient patient = (Patient) authentication.getPrincipal();
            
            // Vérifier que le compte est activé
            if (patient.getAccountStatus() != AccountStatus.ACTIVE) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Account not activated");
                errorResponse.put("message", "Votre compte n'est pas encore activé. Veuillez attendre l'approbation du prestataire de santé.");
                errorResponse.put("accountStatus", patient.getAccountStatus().name());
                errorResponse.put("statusCode", HttpStatus.FORBIDDEN.value());
                
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
            }
            
            // Extraire le token JWT depuis le header Authorization
            String authHeader = request.getHeader("Authorization");
            String jwtToken = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwtToken = authHeader.substring(7);
            }
            
            // Récupérer les dossiers médicaux depuis Medicalrecord-Service
            List<MedicalHistoryResponse> medicalRecords = 
                    medicalRecordClientService.getPatientMedicalRecords(patient.getId(), jwtToken);
            
            return ResponseEntity.ok(medicalRecords);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal server error");
            errorResponse.put("message", "Erreur lors de la récupération de l'historique médical : " + e.getMessage());
            errorResponse.put("statusCode", HttpStatus.INTERNAL_SERVER_ERROR.value());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    //  DTO MAPPER
    private PatientProfileDTO convertToProfileDTO(Patient patient) {
        PatientProfileDTO dto = new PatientProfileDTO();

        dto.setId(patient.getId());
        dto.setEmail(patient.getEmail());
        dto.setAccountStatus(patient.getAccountStatus());

        dto.setFirstName(patient.getPersonalInfo().getFirstName());
        dto.setLastName(patient.getPersonalInfo().getLastName());
        dto.setPhone(patient.getPersonalInfo().getPhone());
        dto.setDateOfBirth(patient.getPersonalInfo().getDateOfBirth());
        dto.setGender(patient.getPersonalInfo().getGender());

        dto.setAddress(patient.getAddressInfo().getAddress());
        dto.setCity(patient.getAddressInfo().getCity());
        dto.setState(patient.getAddressInfo().getState());
        dto.setZipCode(patient.getAddressInfo().getZipCode());
        dto.setCountry(patient.getAddressInfo().getCountry());

        dto.setCreatedAt(patient.getCreatedAt());
        dto.setUpdatedAt(patient.getUpdatedAt());

        return dto;
    }
}
