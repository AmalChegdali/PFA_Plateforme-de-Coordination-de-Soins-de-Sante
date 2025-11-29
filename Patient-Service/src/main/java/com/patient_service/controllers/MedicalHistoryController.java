/* package com.patient_service.controllers;

import com.patient_service.clients.MedicalRecordClient;
import com.patient_service.dto.MedicalHistoryResponse;
import com.patient_service.enums.AccountStatus;
import com.patient_service.models.Patient;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@Tag(name = "Medical Record Gateway", description = "Medical history access via Gateway")
public class MedicalHistoryController {

    @Autowired
    private MedicalRecordClient medicalRecordClient;

    // ✅ GET MEDICAL HISTORY
    @GetMapping("/medical-history")
    @Operation(summary = "Get full medical history (ACTIVE accounts only)")
    public ResponseEntity<?> getMedicalHistory(
            @RequestHeader("Authorization") String token,
            Authentication authentication
    ) {
        try {
            Patient patient = (Patient) authentication.getPrincipal();

            // ✅ Sécurité : uniquement si compte ACTIVE
            if (patient.getAccountStatus() != AccountStatus.ACTIVE) {
                return ResponseEntity
                        .status(403)
                        .body("Access denied: Your account is not ACTIVE.");
            }

            // ✅ Appel vers MedicalRecordService via Feign
            List<MedicalHistoryResponse> history =
                    medicalRecordClient.getMedicalHistory(token);

            return ResponseEntity.ok(history);

        } catch (Exception e) {
            return ResponseEntity
                    .status(500)
                    .body("Error while fetching medical history: " + e.getMessage());
        }
    }
}

 */
