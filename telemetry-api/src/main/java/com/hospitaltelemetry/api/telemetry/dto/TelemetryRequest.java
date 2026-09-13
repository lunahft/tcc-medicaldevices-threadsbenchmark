package com.hospitaltelemetry.api.telemetry.dto;

import java.time.Instant;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import com.hospitaltelemetry.telemetry.MeasurementType;

public record TelemetryRequest(
        @NotNull @Positive Long patientId,
        @NotNull @Positive Long deviceId,
        @NotNull MeasurementType type,
        @NotNull Double value,
        @NotNull Instant measuredAt
) {
}
