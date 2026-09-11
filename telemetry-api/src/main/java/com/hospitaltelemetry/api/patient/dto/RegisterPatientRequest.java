package com.hospitaltelemetry.api.patient.dto;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterPatientRequest(
        @NotBlank String patientCode,
        @NotBlank String unitCode,
        @NotBlank String bedCode,
        @NotNull Instant admittedAt
) {
}
