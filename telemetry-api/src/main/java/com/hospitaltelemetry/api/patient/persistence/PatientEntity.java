package com.hospitaltelemetry.api.patient.persistence;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.hospitaltelemetry.patient.Patient;
import com.hospitaltelemetry.patient.PatientStatus;

@Entity
@Table(name = "patients", schema = "telemetry", uniqueConstraints =
        @UniqueConstraint(name = "uk_patients_patient_code", columnNames = "patient_code"))
public class PatientEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_code", nullable = false, length = 64)
    private String patientCode;

    @Column(name = "unit_code", nullable = false, length = 32)
    private String unitCode;

    @Column(name = "bed_code", nullable = false, length = 32)
    private String bedCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PatientStatus status;

    @Column(name = "admitted_at", nullable = false)
    private Instant admittedAt;

    @Column(name = "discharged_at")
    private Instant dischargedAt;

    protected PatientEntity() {
    }

    static PatientEntity from(Patient patient) {
        PatientEntity entity = new PatientEntity();
        entity.id = patient.getId();
        entity.patientCode = patient.getPatientCode();
        entity.unitCode = patient.getUnitCode();
        entity.bedCode = patient.getBedCode();
        entity.status = patient.getStatus();
        entity.admittedAt = patient.getAdmittedAt();
        entity.dischargedAt = patient.getDischargedAt();
        return entity;
    }

    Patient toDomain() {
        return Patient.restore(id, patientCode, unitCode, bedCode, status, admittedAt, dischargedAt);
    }
}
