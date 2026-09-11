package com.hospitaltelemetry.api.patient.dto;

import java.time.Instant;

import com.hospitaltelemetry.patient.Patient;
import com.hospitaltelemetry.patient.PatientStatus;

public record PatientResponse(
        Long id,
        String patientCode,
        String unitCode,
        String bedCode,
        PatientStatus status,
        Instant admittedAt,
        Instant dischargedAt
) {

    public static PatientResponse from(Patient patient) {
        return new PatientResponse(
                patient.getId(),
                patient.getPatientCode(),
                patient.getUnitCode(),
                patient.getBedCode(),
                patient.getStatus(),
                patient.getAdmittedAt(),
                patient.getDischargedAt()
        );
    }
}
