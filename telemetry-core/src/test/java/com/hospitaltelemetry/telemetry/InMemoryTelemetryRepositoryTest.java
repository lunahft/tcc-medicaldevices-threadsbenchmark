package com.hospitaltelemetry.telemetry;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class InMemoryTelemetryRepositoryTest {

    @Test
    void save_validEvent_shouldAssignIdAndIncreaseCount() {
        InMemoryTelemetryRepository repository = new InMemoryTelemetryRepository();
        TelemetryEvent event = createEvent(
            1L,
            10L,
            MeasurementType.HEART_RATE,
            75.0,
            Instant.parse("2026-01-01T10:00:00Z")
        );

        TelemetryEvent savedEvent = repository.save(event);

        assertSame(event, savedEvent);
        assertEquals(1L, savedEvent.getId());
        assertEquals(1, repository.count());
    }

    @Test
    void save_nullEvent_shouldThrowIllegalArgumentException() {
        InMemoryTelemetryRepository repository = new InMemoryTelemetryRepository();

        assertThrows(IllegalArgumentException.class,() -> repository.save(null));
    }

    @Test
    void findLatestByPatientIdAndType_matchingEvents_shouldReturnMostRecentMeasuredAt() {
        InMemoryTelemetryRepository repository = new InMemoryTelemetryRepository();
        TelemetryEvent olderHeartRate = createEvent(
            1L,
            10L,
            MeasurementType.HEART_RATE,
            70.0,
            Instant.parse("2026-01-01T10:00:00Z")
        );
        TelemetryEvent latestHeartRate = createEvent(
            1L,
            10L,
            MeasurementType.HEART_RATE,
            82.0,
            Instant.parse("2026-01-01T10:05:00Z")
        );
        TelemetryEvent otherPatientHeartRate = createEvent(
            2L,
            20L,
            MeasurementType.HEART_RATE,
            95.0,
            Instant.parse("2026-01-01T10:10:00Z")
        );
        TelemetryEvent otherType = createEvent(
            1L,
            10L,
            MeasurementType.OXYGEN_SATURATION,
            98.0,
            Instant.parse("2026-01-01T10:15:00Z")
        );
        repository.save(olderHeartRate);
        repository.save(latestHeartRate);
        repository.save(otherPatientHeartRate);
        repository.save(otherType);

        TelemetryEvent result = repository.findLatestByPatientIdAndType(1L, MeasurementType.HEART_RATE)
            .orElseThrow();

        assertSame(latestHeartRate, result);
    }

    @Test
    void findLatestByPatientIdAndType_noMatchingEvents_shouldReturnEmptyOptional() {
        InMemoryTelemetryRepository repository = new InMemoryTelemetryRepository();
        repository.save(createEvent(
            1L,
            10L,
            MeasurementType.HEART_RATE,
            75.0,
            Instant.parse("2026-01-01T10:00:00Z")
        ));

        assertTrue(repository.findLatestByPatientIdAndType(2L, MeasurementType.HEART_RATE).isEmpty());
        assertTrue(repository.findLatestByPatientIdAndType(1L, MeasurementType.OXYGEN_SATURATION).isEmpty());
    }

    @Test
    void findLatestByPatientIdAndType_invalidParameters_shouldReturnEmptyOptional() {
        InMemoryTelemetryRepository repository = new InMemoryTelemetryRepository();

        assertTrue(repository.findLatestByPatientIdAndType(null, MeasurementType.HEART_RATE).isEmpty());
        assertTrue(repository.findLatestByPatientIdAndType(0L, MeasurementType.HEART_RATE).isEmpty());
        assertTrue(repository.findLatestByPatientIdAndType(1L, null).isEmpty());
    }

    @Test
    void findByPatientIdAndPeriod_matchingEvents_shouldReturnEventsOrderedByMeasuredAt() {
        InMemoryTelemetryRepository repository = new InMemoryTelemetryRepository();
        TelemetryEvent startBoundary = createEvent(
            1L,
            10L,
            MeasurementType.HEART_RATE,
            70.0,
            Instant.parse("2026-01-01T10:00:00Z")
        );
        TelemetryEvent middle = createEvent(
            1L,
            10L,
            MeasurementType.OXYGEN_SATURATION,
            98.0,
            Instant.parse("2026-01-01T10:05:00Z")
        );
        TelemetryEvent endBoundary = createEvent(
            1L,
            10L,
            MeasurementType.BODY_TEMPERATURE,
            36.5,
            Instant.parse("2026-01-01T10:10:00Z")
        );
        TelemetryEvent beforePeriod = createEvent(
            1L,
            10L,
            MeasurementType.HEART_RATE,
            68.0,
            Instant.parse("2026-01-01T09:59:59Z")
        );
        TelemetryEvent afterPeriod = createEvent(
            1L,
            10L,
            MeasurementType.HEART_RATE,
            85.0,
            Instant.parse("2026-01-01T10:10:01Z")
        );
        TelemetryEvent otherPatient = createEvent(
            2L,
            20L,
            MeasurementType.HEART_RATE,
            90.0,
            Instant.parse("2026-01-01T10:05:00Z")
        );
        repository.save(middle);
        repository.save(afterPeriod);
        repository.save(endBoundary);
        repository.save(otherPatient);
        repository.save(beforePeriod);
        repository.save(startBoundary);

        List<TelemetryEvent> result = repository.findByPatientIdAndPeriod(
            1L,
            Instant.parse("2026-01-01T10:00:00Z"),
            Instant.parse("2026-01-01T10:10:00Z")
        );

        assertEquals(List.of(startBoundary, middle, endBoundary), result);
    }

    @Test
    void findByPatientIdAndPeriod_invalidPatientId_shouldReturnEmptyList() {
        InMemoryTelemetryRepository repository = new InMemoryTelemetryRepository();

        assertTrue(repository.findByPatientIdAndPeriod(
            null,
            Instant.parse("2026-01-01T10:00:00Z"),
            Instant.parse("2026-01-01T10:10:00Z")
        ).isEmpty());

        assertTrue(repository.findByPatientIdAndPeriod(
            0L,
            Instant.parse("2026-01-01T10:00:00Z"),
            Instant.parse("2026-01-01T10:10:00Z")
        ).isEmpty());
    }

    @Test
    void findByPatientIdAndPeriod_nullPeriodBoundary_shouldThrowIllegalArgumentException() {
        InMemoryTelemetryRepository repository = new InMemoryTelemetryRepository();
        Instant instant = Instant.parse("2026-01-01T10:00:00Z");

        assertThrows(IllegalArgumentException.class,() -> repository.findByPatientIdAndPeriod(1L, null, instant));
        assertThrows(IllegalArgumentException.class,() -> repository.findByPatientIdAndPeriod(1L, instant, null));
    }

    @Test
    void findByPatientIdAndPeriod_endBeforeStart_shouldThrowIllegalArgumentException() {
        InMemoryTelemetryRepository repository = new InMemoryTelemetryRepository();

        assertThrows(IllegalArgumentException.class,() -> repository.findByPatientIdAndPeriod(
                1L,
                Instant.parse("2026-01-01T10:10:00Z"),
                Instant.parse("2026-01-01T10:00:00Z")
            )
        );
    }

    @Test
    void count_emptyRepository_shouldReturnZero() {
        InMemoryTelemetryRepository repository = new InMemoryTelemetryRepository();

        assertEquals(0, repository.count());
    }

    private static TelemetryEvent createEvent(
        Long patientId,
        Long deviceId,
        MeasurementType type,
        double value,
        Instant measuredAt
    ) {
        return new TelemetryEvent(
            patientId,
            deviceId,
            type,
            value,
            measuredAt,
            measuredAt.plusSeconds(1)
        );
    }
}
