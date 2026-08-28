package com.hospitaltelemetry.telemetry;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.Instant;


public class TelemetryEventTest {

    @Test
    void constructor_validParameters_shouldCreateTelemetryEvent() {
        Instant measuredAt = Instant.now();
        Instant receivedAt = measuredAt.plusSeconds(5);

        TelemetryEvent event = new TelemetryEvent(
            1L,
            2L,
            MeasurementType.HEART_RATE,
            75.0,
            measuredAt,
            receivedAt
        );

        assertNotNull(event);
        assertEquals(1L, event.getPatientId());
        assertEquals(2L, event.getDeviceId());
        assertEquals(MeasurementType.HEART_RATE, event.getType());
        assertEquals(75.0, event.getValue());
        assertEquals(measuredAt, event.getMeasuredAt());
        assertEquals(receivedAt, event.getReceivedAt());
    }

    @Test
    void validateId_nullId_shouldThrowIllegalArgumentException() {
        assertThrows(
            IllegalArgumentException.class, () -> new TelemetryEvent(
                null,
                1L,
                MeasurementType.HEART_RATE,
                75.0,
                Instant.now(),
                Instant.now()
            )
        );
    }

    @Test
    void validateId_negativeId_shouldThrowIllegalArgumentException() {
        assertThrows(
            IllegalArgumentException.class, () -> new TelemetryEvent(
                -1L,
                1L,
                MeasurementType.HEART_RATE,
                75.0,
                Instant.now(),
                Instant.now()
            )
        );
    }

    @Test
    void validateChronology_receivedAtBeforeMeasuredAt_shouldThrowIllegalArgumentException() {
        Instant measuredAt = Instant.now();
        Instant receivedAt = measuredAt.minusSeconds(10);

        assertThrows(
            IllegalArgumentException.class, () -> new TelemetryEvent(
                1L,
                1L,
                MeasurementType.HEART_RATE,
                75.0,
                measuredAt,
                receivedAt
            )
        );
    }

    @Test
    void validateValue_nonFiniteValue_shouldThrowIllegalArgumentException() {
        assertThrows(
            IllegalArgumentException.class, () -> new TelemetryEvent(
                1L,
                1L,
                MeasurementType.HEART_RATE,
                Double.POSITIVE_INFINITY,
                Instant.now(),
                Instant.now()
            )
        );

        assertThrows(
            IllegalArgumentException.class, () -> new TelemetryEvent(
                1L,
                1L,
                MeasurementType.HEART_RATE,
                Double.NaN,
                Instant.now(),
                Instant.now()
            )
        );

        assertThrows(
            IllegalArgumentException.class, () -> new TelemetryEvent(
                1L,
                1L,
                MeasurementType.HEART_RATE,
                Double.NEGATIVE_INFINITY,
                Instant.now(),
                Instant.now()
            )
        );
    }

    @Test
    void validateValue_finiteValue_shouldNotThrowException() {
        assertDoesNotThrow(() -> new TelemetryEvent(
            1L,
            1L,
            MeasurementType.HEART_RATE,
            75.0,
            Instant.now(),
            Instant.now()
        ));
    }

    @Test
    void validateType_nullType_shouldThrowIllegalArgumentException() {
        assertThrows(
            IllegalArgumentException.class, () -> new TelemetryEvent(
                1L,
                1L,
                null,
                75.0,
                Instant.now(),
                Instant.now()
            )
        );
    }

    @Test
    void validateInstant_nullInstant_shouldThrowIllegalArgumentException() {
        assertThrows(
            IllegalArgumentException.class, () -> new TelemetryEvent(
                1L,
                1L,
                MeasurementType.HEART_RATE,
                75.0,
                null,
                Instant.now()
            )
        );

        assertThrows(
            IllegalArgumentException.class, () -> new TelemetryEvent(
                1L,
                1L,
                MeasurementType.HEART_RATE,
                75.0,
                Instant.now(),
                null
            )
        );
    }

    @Test
    void validateChronology_receivedAtEqualToMeasuredAt_shouldNotThrowException() {
        Instant now = Instant.now();
        assertDoesNotThrow(() -> new TelemetryEvent(
            1L,
            1L,
            MeasurementType.HEART_RATE,
            75.0,
            now,
            now
        ));
    }

    @Test
    void validateId_nullDeviceId_shouldThrowIllegalArgumentException() {
        assertThrows(
            IllegalArgumentException.class, () -> new TelemetryEvent(
                1L,
                null,
                MeasurementType.HEART_RATE,
                75.0,
                Instant.now(),
                Instant.now()
            )
        );
    }

    @Test
    void validateId_negativeDeviceId_shouldThrowIllegalArgumentException() {
        assertThrows(
            IllegalArgumentException.class, () -> new TelemetryEvent(
                1L,
                -1L,
                MeasurementType.HEART_RATE,
                75.0,
                Instant.now(),
                Instant.now()
            )
        );
    }
}
