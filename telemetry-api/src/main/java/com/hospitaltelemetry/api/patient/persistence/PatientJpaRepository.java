package com.hospitaltelemetry.api.patient.persistence;

import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

@Profile("postgres")
public interface PatientJpaRepository extends JpaRepository<PatientEntity, Long> {

    Optional<PatientEntity> findByPatientCode(String patientCode);

    boolean existsByPatientCode(String patientCode);
}
