package com.hospitaltelemetry.telemetry;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TelemetryRepository {

    TelemetryEvent save(TelemetryEvent telemetryEvent);

    Optional<TelemetryEvent> findLatestByPatientIdAndType(
        Long patientId,
        MeasurementType type
    );

    List<TelemetryEvent> findByPatientIdAndPeriod(
        Long patientId,
        Instant start,
        Instant end
    );

    long count();
}
