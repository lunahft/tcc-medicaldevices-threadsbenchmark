package com.hospitaltelemetry.telemetry;

import com.hospitaltelemetry.patient.InMemoryPatientRepository;
import com.hospitaltelemetry.patient.Patient;
import com.hospitaltelemetry.patient.PatientRepository;
import com.hospitaltelemetry.patient.exception.PatientNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class TelemetryQueryServiceTest {
    private TelemetryQueryService telemetryQueryService;
    private TelemetryRepository telemetryRepository;
    private PatientRepository patientRepository;

    @BeforeEach
    void setUp(){
        telemetryRepository = new InMemoryTelemetryRepository();
        patientRepository = new InMemoryPatientRepository();
        telemetryQueryService = new TelemetryQueryService(
            telemetryRepository,
            patientRepository
        );
    }

    @Test
    void constructor_nullTelemetryRepository_shouldThrowIllegalArgumentException(){
        assertThrows(
            IllegalArgumentException.class,
            () -> new TelemetryQueryService(null, patientRepository)
        );
    }

    @Test
    void constructor_nullPatientRepository_shouldThrowIllegalArgumentException(){
        assertThrows(
            IllegalArgumentException.class,
            () -> new TelemetryQueryService(telemetryRepository, null)
        );
    }

    @Test
    void getLatestMeasurement_existingPatientAndMatchingMeasurement_shouldReturnLatestEvent(){
        Patient patient = savePatient("PAT-001");
        TelemetryEvent olderHeartRate = saveEvent(
            patient.getId(),
            10L,
            MeasurementType.HEART_RATE,
            70.0,
            Instant.parse("2026-01-01T10:00:00Z")
        );
        TelemetryEvent latestHeartRate = saveEvent(
            patient.getId(),
            10L,
            MeasurementType.HEART_RATE,
            82.0,
            Instant.parse("2026-01-01T10:05:00Z")
        );
        saveEvent(
            patient.getId(),
            10L,
            MeasurementType.OXYGEN_SATURATION,
            98.0,
            Instant.parse("2026-01-01T10:10:00Z")
        );

        Optional<TelemetryEvent> result = telemetryQueryService.getLatestMeasurement(
            patient.getId(),
            MeasurementType.HEART_RATE
        );

        assertTrue(result.isPresent());
        assertNotSame(olderHeartRate, result.get());
        assertSame(latestHeartRate, result.get());
    }

    @Test
    void getLatestMeasurement_existingPatientAndNoMatchingMeasurement_shouldReturnEmptyOptional(){
        Patient patient = savePatient("PAT-001");
        saveEvent(
            patient.getId(),
            10L,
            MeasurementType.HEART_RATE,
            75.0,
            Instant.parse("2026-01-01T10:00:00Z")
        );

        Optional<TelemetryEvent> result = telemetryQueryService.getLatestMeasurement(
            patient.getId(),
            MeasurementType.OXYGEN_SATURATION
        );

        assertTrue(result.isEmpty());
    }

    @Test
    void getLatestMeasurement_missingPatient_shouldThrowPatientNotFoundException(){
        assertThrows(
            PatientNotFoundException.class,
            () -> telemetryQueryService.getLatestMeasurement(1L, MeasurementType.HEART_RATE)
        );
    }

    @Test
    void getHistory_existingPatientAndMatchingPeriod_shouldReturnEventsOrderedByMeasuredAt(){
        Patient patient = savePatient("PAT-001");
        TelemetryEvent startBoundary = saveEvent(
            patient.getId(),
            10L,
            MeasurementType.HEART_RATE,
            70.0,
            Instant.parse("2026-01-01T10:00:00Z")
        );
        TelemetryEvent middle = saveEvent(
            patient.getId(),
            10L,
            MeasurementType.OXYGEN_SATURATION,
            98.0,
            Instant.parse("2026-01-01T10:05:00Z")
        );
        TelemetryEvent endBoundary = saveEvent(
            patient.getId(),
            10L,
            MeasurementType.BODY_TEMPERATURE,
            36.5,
            Instant.parse("2026-01-01T10:10:00Z")
        );
        saveEvent(
            patient.getId(),
            10L,
            MeasurementType.HEART_RATE,
            68.0,
            Instant.parse("2026-01-01T09:59:59Z")
        );
        saveEvent(
            patient.getId(),
            10L,
            MeasurementType.HEART_RATE,
            85.0,
            Instant.parse("2026-01-01T10:10:01Z")
        );

        List<TelemetryEvent> result = telemetryQueryService.getHistory(
            patient.getId(),
            Instant.parse("2026-01-01T10:00:00Z"),
            Instant.parse("2026-01-01T10:10:00Z")
        );

        assertEquals(List.of(startBoundary, middle, endBoundary), result);
    }

    @Test
    void getHistory_existingPatientAndNoMatchingEvents_shouldReturnEmptyList(){
        Patient patient = savePatient("PAT-001");
        saveEvent(
            patient.getId(),
            10L,
            MeasurementType.HEART_RATE,
            75.0,
            Instant.parse("2026-01-01T10:00:00Z")
        );

        List<TelemetryEvent> result = telemetryQueryService.getHistory(
            patient.getId(),
            Instant.parse("2026-01-01T11:00:00Z"),
            Instant.parse("2026-01-01T11:10:00Z")
        );

        assertTrue(result.isEmpty());
    }

    @Test
    void getHistory_missingPatient_shouldThrowPatientNotFoundException(){
        assertThrows(
            PatientNotFoundException.class,
            () -> telemetryQueryService.getHistory(
                1L,
                Instant.parse("2026-01-01T10:00:00Z"),
                Instant.parse("2026-01-01T10:10:00Z")
            )
        );
    }

    @Test
    void getHistory_nullPeriodBoundary_shouldThrowIllegalArgumentException(){
        Patient patient = savePatient("PAT-001");
        Instant instant = Instant.parse("2026-01-01T10:00:00Z");

        assertThrows(
            IllegalArgumentException.class,
            () -> telemetryQueryService.getHistory(patient.getId(), null, instant)
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> telemetryQueryService.getHistory(patient.getId(), instant, null)
        );
    }

    @Test
    void getHistory_endBeforeStart_shouldThrowIllegalArgumentException(){
        Patient patient = savePatient("PAT-001");

        assertThrows(
            IllegalArgumentException.class,
            () -> telemetryQueryService.getHistory(
                patient.getId(),
                Instant.parse("2026-01-01T10:10:00Z"),
                Instant.parse("2026-01-01T10:00:00Z")
            )
        );
    }

    @Test
    void getSnapshot_existingPatientWithAllMeasurements_shouldReturnLatestEventsByType(){
        Patient patient = savePatient("PAT-001");
        saveEvent(
            patient.getId(),
            10L,
            MeasurementType.HEART_RATE,
            70.0,
            Instant.parse("2026-01-01T10:00:00Z")
        );
        TelemetryEvent latestHeartRate = saveEvent(
            patient.getId(),
            10L,
            MeasurementType.HEART_RATE,
            82.0,
            Instant.parse("2026-01-01T10:05:00Z")
        );
        TelemetryEvent oxygenSaturation = saveEvent(
            patient.getId(),
            10L,
            MeasurementType.OXYGEN_SATURATION,
            98.0,
            Instant.parse("2026-01-01T10:01:00Z")
        );
        TelemetryEvent bodyTemperature = saveEvent(
            patient.getId(),
            10L,
            MeasurementType.BODY_TEMPERATURE,
            36.5,
            Instant.parse("2026-01-01T10:02:00Z")
        );
        TelemetryEvent respiratoryRate = saveEvent(
            patient.getId(),
            10L,
            MeasurementType.RESPIRATORY_RATE,
            18.0,
            Instant.parse("2026-01-01T10:03:00Z")
        );
        TelemetryEvent systolicBloodPressure = saveEvent(
            patient.getId(),
            10L,
            MeasurementType.SYSTOLIC_BLOOD_PRESSURE,
            120.0,
            Instant.parse("2026-01-01T10:04:00Z")
        );
        TelemetryEvent diastolicBloodPressure = saveEvent(
            patient.getId(),
            10L,
            MeasurementType.DIASTOLIC_BLOOD_PRESSURE,
            80.0,
            Instant.parse("2026-01-01T10:04:00Z")
        );

        PatientTelemetrySnapshot snapshot = telemetryQueryService.getSnapshot(patient.getId());

        assertEquals(patient.getId(), snapshot.getPatientId());
        assertSame(latestHeartRate, snapshot.getHeartRate());
        assertSame(oxygenSaturation, snapshot.getOxygenSaturation());
        assertSame(bodyTemperature, snapshot.getBodyTemperature());
        assertSame(respiratoryRate, snapshot.getRespiratoryRate());
        assertSame(systolicBloodPressure, snapshot.getSystolicBloodPressure());
        assertSame(diastolicBloodPressure, snapshot.getDiastolicBloodPressure());
    }

    @Test
    void getSnapshot_existingPatientWithPartialMeasurements_shouldReturnNullForMissingTypes(){
        Patient patient = savePatient("PAT-001");
        TelemetryEvent heartRate = saveEvent(
            patient.getId(),
            10L,
            MeasurementType.HEART_RATE,
            75.0,
            Instant.parse("2026-01-01T10:00:00Z")
        );

        PatientTelemetrySnapshot snapshot = telemetryQueryService.getSnapshot(patient.getId());

        assertEquals(patient.getId(), snapshot.getPatientId());
        assertSame(heartRate, snapshot.getHeartRate());
        assertNull(snapshot.getOxygenSaturation());
        assertNull(snapshot.getBodyTemperature());
        assertNull(snapshot.getRespiratoryRate());
        assertNull(snapshot.getSystolicBloodPressure());
        assertNull(snapshot.getDiastolicBloodPressure());
    }

    @Test
    void getSnapshot_missingPatient_shouldThrowPatientNotFoundException(){
        assertThrows(
            PatientNotFoundException.class,
            () -> telemetryQueryService.getSnapshot(1L)
        );
    }

    private TelemetryEvent saveEvent(
        Long patientId,
        Long deviceId,
        MeasurementType type,
        double value,
        Instant measuredAt
    ) {
        TelemetryEvent event = new TelemetryEvent(
            patientId,
            deviceId,
            type,
            value,
            measuredAt,
            measuredAt.plusSeconds(1)
        );

        return telemetryRepository.save(event);
    }

    private Patient savePatient(String patientCode){
        Patient patient = new Patient(
            patientCode,
            "ICU-A",
            "BED-01",
            Instant.parse("2026-01-01T09:00:00Z")
        );

        return patientRepository.save(patient);
    }
}
