package com.hospitaltelemetry.patient;

import java.util.List;
import java.util.Optional;

public interface PatientRepository {

    Patient save(Patient patient);
    Optional<Patient> findById(Long id);
    Optional<Patient> findByPatientCode(String patientCode);
    boolean existsByPatientCode(String patientCode);
    
    List<Patient> findAll();
}
