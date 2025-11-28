package com.provider_service.services;

import com.provider_service.dto.PatientDTO;
import com.provider_service.enums.AccountStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProviderPatientService {

    private final List<PatientDTO> patients = new ArrayList<>();

    public ProviderPatientService() {
        // Données fictives pour test
        patients.add(new PatientDTO("1", "Amal Chegdali", "amal@gmail.com", "0600000000", AccountStatus.PENDING));
        patients.add(new PatientDTO("2", "John Doe", "john@gmail.com", "0600111222", AccountStatus.ACTIVE));
    }

    public List<PatientDTO> getPatients(String providerId, String status) {
        if ("ALL".equalsIgnoreCase(status)) {
            return patients;
        }
        AccountStatus filterStatus;
        try {
            filterStatus = AccountStatus.valueOf(status.toUpperCase());
        } catch (Exception e) {
            filterStatus = AccountStatus.PENDING;
        }
        AccountStatus finalFilterStatus = filterStatus;
        return patients.stream()
                .filter(p -> p.getAccountStatus() == finalFilterStatus)
                .collect(Collectors.toList());
    }

    public PatientDTO getPatientById(String providerId, String patientId) {
        return patients.stream()
                .filter(p -> p.getId().equals(patientId))
                .findFirst()
                .orElse(null);
    }
}
