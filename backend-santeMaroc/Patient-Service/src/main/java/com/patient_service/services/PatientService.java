package com.patient_service.services;

import com.patient_service.dto.PatientDTO;
import com.patient_service.dto.ProfileCompletionRequest;
import com.patient_service.dto.ProfileStatusResponse;
import com.patient_service.dto.RegisterRequest;
import com.patient_service.enums.AccountStatus;
import com.patient_service.models.Patient;
import com.patient_service.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientService implements UserDetailsService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PatientPublisherService patientPublisherService; // Service pour publier le patient à RabbitMQ

    @Autowired
    private PatientMapper mapper; // Mapper pour convertir Patient -> PatientDTO

    // ------------------- UserDetailsService -------------------
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return patientRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Patient not found with email: " + email));
    }

    // ------------------- Registration -------------------
    public Patient registerPatient(String email, String password, RegisterRequest registerRequest) {
        // Vérifier si le patient existe déjà
        if (patientRepository.existsByEmail(email)) {
            throw new RuntimeException("Patient already exists with email: " + email);
        }

        // Créer un nouveau patient
        Patient patient = new Patient();
        patient.setEmail(email);
        patient.setPassword(passwordEncoder.encode(password));
        patient.setAccountStatus(AccountStatus.PENDING); // Par défaut en attente

        // Remplir les informations du registerRequest
        if (registerRequest.getFirstName() != null) patient.setFirstName(registerRequest.getFirstName());
        if (registerRequest.getLastName() != null) patient.setLastName(registerRequest.getLastName());
        if (registerRequest.getPhone() != null) patient.setPhone(registerRequest.getPhone());
        if (registerRequest.getDateOfBirth() != null) patient.setDateOfBirth(registerRequest.getDateOfBirth());
        if (registerRequest.getGender() != null) patient.setGender(registerRequest.getGender());
        if (registerRequest.getAddress() != null) patient.setAddress(registerRequest.getAddress());
        if (registerRequest.getCity() != null) patient.setCity(registerRequest.getCity());
        if (registerRequest.getState() != null) patient.setState(registerRequest.getState());
        if (registerRequest.getZipCode() != null) patient.setZipCode(registerRequest.getZipCode());
        if (registerRequest.getCountry() != null) patient.setCountry(registerRequest.getCountry());

        // Sauvegarder le patient
        Patient savedPatient = patientRepository.save(patient);

        // Convertir le patient en DTO et publier à RabbitMQ
        PatientDTO dto = mapper.toDto(savedPatient);
        patientPublisherService.publishPatient(dto);

        return savedPatient;
    }

    // ------------------- Complete Profile -------------------
    public Patient completePatientProfile(String patientId, ProfileCompletionRequest request) {
        Patient patient = findById(patientId);

        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setPhone(request.getPhone());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setGender(request.getGender());
        patient.setAddress(request.getAddress());
        patient.setCity(request.getCity());
        patient.setState(request.getState());
        patient.setZipCode(request.getZipCode());
        patient.setCountry(request.getCountry());

        return patientRepository.save(patient);
    }

    // ------------------- Profile Status -------------------
    public ProfileStatusResponse getProfileStatus(String patientId) {
        Patient patient = findById(patientId);

        ProfileStatusResponse response = new ProfileStatusResponse(
                patient.getId(), patient.getEmail(), patient.getAccountStatus()
        );

        // Vérifier si chaque section du profil est complétée
        boolean hasPersonalInfo = patient.getFirstName() != null && patient.getLastName() != null && patient.getDateOfBirth() != null;
        boolean hasContactInfo = patient.getPhone() != null && patient.getEmail() != null;
        boolean hasAddressInfo = patient.getAddress() != null && patient.getCity() != null;

        response.setHasPersonalInfo(hasPersonalInfo);
        response.setHasContactInfo(hasContactInfo);
        response.setHasAddressInfo(hasAddressInfo);

        boolean basicProfileComplete = hasPersonalInfo && hasContactInfo;
        response.setBasicProfileComplete(basicProfileComplete);

        // Calculer le pourcentage de complétion du profil
        int completedSections = 0;
        int totalSections = 5; // personal, contact, address, medical, emergency
        if (hasPersonalInfo) completedSections++;
        if (hasContactInfo) completedSections++;
        if (hasAddressInfo) completedSections++;
        response.setCompletionPercentage((completedSections * 100) / totalSections);

        // Définir le message et la prochaine étape selon le statut du compte
        if (patient.getAccountStatus() == AccountStatus.PENDING) {
            if (!basicProfileComplete) {
                response.setNextStep("Complete your basic profile information");
                response.setMessage("Please complete your personal details to help providers serve you better");
            } else {
                response.setNextStep("Wait for provider activation");
                response.setMessage("Your profile is complete. Waiting for provider to activate your account");
            }
        } else if (patient.getAccountStatus() == AccountStatus.ACTIVE) {
            response.setNextStep("Profile complete - Account active");
            response.setMessage("Your account is active and you can access all services");
        }

        return response;
    }

    // ------------------- Helper Methods -------------------
    public boolean canAccessMedicalHistory(Patient patient) {
        return patient.getAccountStatus() == AccountStatus.ACTIVE;
    }

    public Patient findByEmail(String email) {
        return patientRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Patient not found with email: " + email));
    }

    public Patient findById(String patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + patientId));
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    // ------------------- Account Management -------------------
    public Patient activatePatient(String patientId) {
        Patient patient = findById(patientId);
        patient.setAccountStatus(AccountStatus.ACTIVE);
        return patientRepository.save(patient);
    }

    public Patient deactivatePatient(String patientId) {
        Patient patient = findById(patientId);
        patient.setAccountStatus(AccountStatus.INACTIVE);
        return patientRepository.save(patient);
    }

    public Patient updatePatientProfile(String patientId, Patient profileUpdates) {
        Patient existingPatient = findById(patientId);

        if (profileUpdates.getFirstName() != null) existingPatient.setFirstName(profileUpdates.getFirstName());
        if (profileUpdates.getLastName() != null) existingPatient.setLastName(profileUpdates.getLastName());
        if (profileUpdates.getPhone() != null) existingPatient.setPhone(profileUpdates.getPhone());
        if (profileUpdates.getDateOfBirth() != null) existingPatient.setDateOfBirth(profileUpdates.getDateOfBirth());
        if (profileUpdates.getGender() != null) existingPatient.setGender(profileUpdates.getGender());
        if (profileUpdates.getAddress() != null) existingPatient.setAddress(profileUpdates.getAddress());
        if (profileUpdates.getCity() != null) existingPatient.setCity(profileUpdates.getCity());
        if (profileUpdates.getState() != null) existingPatient.setState(profileUpdates.getState());
        if (profileUpdates.getZipCode() != null) existingPatient.setZipCode(profileUpdates.getZipCode());
        if (profileUpdates.getCountry() != null) existingPatient.setCountry(profileUpdates.getCountry());

        return patientRepository.save(existingPatient);
    }
}
