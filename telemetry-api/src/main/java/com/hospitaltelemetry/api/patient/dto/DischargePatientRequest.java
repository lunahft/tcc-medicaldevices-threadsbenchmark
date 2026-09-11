package com.hospitaltelemetry.api.patient.dto;

import java.time.Instant;

import jakarta.validation.constraints.NotNull;

public record DischargePatientRequest(
        @NotNull Instant dischargedAt
) {
}
