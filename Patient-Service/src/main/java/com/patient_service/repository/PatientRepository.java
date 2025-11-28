package com.patient_service.repository;

import java.util.List;
import java.util.Optional;

import com.patient_service.enums.AccountStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.patient_service.models.Patient;

@Repository
public interface PatientRepository extends MongoRepository<Patient, String>{
	Optional<Patient> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    List<Patient> findByAccountStatus(AccountStatus accountStatus);
}
