package com.hospitaltelemetry.api.patient.persistence;

import java.util.List;
import java.util.Optional;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import com.hospitaltelemetry.patient.Patient;
import com.hospitaltelemetry.patient.PatientRepository;
import com.hospitaltelemetry.patient.exception.DuplicatePatientCodeException;

@Repository
@Profile("postgres")
public class PostgresPatientRepository implements PatientRepository {

    private final PatientJpaRepository patientJpaRepository;

    public PostgresPatientRepository(PatientJpaRepository patientJpaRepository) {
        this.patientJpaRepository = patientJpaRepository;
    }

    @Override
    public Patient save(Patient patient) {
        if (patient == null) {
            throw new IllegalArgumentException("patient can't be null");
        }
        if (patient.getId() != null) {
            throw new IllegalStateException("Patient already has an id");
        }

        try {
            return patientJpaRepository.saveAndFlush(PatientEntity.from(patient)).toDomain();
        } catch (DataIntegrityViolationException exception) {
            for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
                if (cause instanceof ConstraintViolationException violation
                        && "uk_patients_patient_code".equals(violation.getConstraintName())) {
                    throw new DuplicatePatientCodeException(patient.getPatientCode());
                }
            }
            throw exception;
        }
    }

    @Override
    public Optional<Patient> findById(Long id) {
        if (id == null || id <= 0) {
            return Optional.empty();
        }
        return patientJpaRepository.findById(id).map(PatientEntity::toDomain);
    }

    @Override
    public Optional<Patient> findByPatientCode(String patientCode) {
        if (patientCode == null || patientCode.isBlank()) {
            return Optional.empty();
        }
        return patientJpaRepository.findByPatientCode(patientCode).map(PatientEntity::toDomain);
    }

    @Override
    public boolean existsByPatientCode(String patientCode) {
        if (patientCode == null || patientCode.isBlank()) {
            return false;
        }
        return patientJpaRepository.existsByPatientCode(patientCode);
    }

    @Override
    public List<Patient> findAll() {
        return patientJpaRepository.findAll().stream().map(PatientEntity::toDomain).toList();
    }
}
