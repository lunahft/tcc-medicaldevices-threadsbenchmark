package com.hospitaltelemetry.api.telemetry.dto;

import java.time.Instant;

import com.hospitaltelemetry.telemetry.MeasurementType;
import com.hospitaltelemetry.telemetry.MeasurementUnit;
import com.hospitaltelemetry.telemetry.TelemetryEvent;

public record TelemetryResponse(
        Long id,
        Long patientId,
        Long deviceId,
        MeasurementType type,
        MeasurementUnit unit,
        double value,
        Instant measuredAt,
        Instant receivedAt
) {

    public static TelemetryResponse from(TelemetryEvent event) {
        return new TelemetryResponse(
                event.getId(),
                event.getPatientId(),
                event.getDeviceId(),
                event.getType(),
                event.getType().getUnit(),
                event.getValue(),
                event.getMeasuredAt(),
                event.getReceivedAt()
        );
    }
}
