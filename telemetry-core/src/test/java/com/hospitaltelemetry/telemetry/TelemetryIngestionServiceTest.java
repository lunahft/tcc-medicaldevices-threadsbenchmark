package com.hospitaltelemetry.telemetry;

import com.hospitaltelemetry.device.DeviceRepository;
import com.hospitaltelemetry.device.DeviceStatus;
import com.hospitaltelemetry.device.DeviceType;
import com.hospitaltelemetry.device.InMemoryDeviceRepository;
import com.hospitaltelemetry.device.MonitoringDevice;
import com.hospitaltelemetry.device.exception.DeviceNotAssignedException;
import com.hospitaltelemetry.device.exception.DeviceNotFoundException;
import com.hospitaltelemetry.patient.InMemoryPatientRepository;
import com.hospitaltelemetry.patient.Patient;
import com.hospitaltelemetry.patient.PatientRepository;
import com.hospitaltelemetry.patient.exception.PatientNotFoundException;
import com.hospitaltelemetry.telemetry.exception.InvalidTelemetryException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class TelemetryIngestionServiceTest {
    private TelemetryIngestionService telemetryIngestionService;
    private DeviceRepository deviceRepository;
    private PatientRepository patientRepository;
    private TelemetryRepository telemetryRepository;

    @BeforeEach
    void setUp(){
        deviceRepository = new InMemoryDeviceRepository();
        patientRepository = new InMemoryPatientRepository();
        telemetryRepository = new InMemoryTelemetryRepository();
        telemetryIngestionService = new TelemetryIngestionService(
            deviceRepository,
            patientRepository,
            telemetryRepository
        );
    }

    @Test
    void constructor_nullDeviceRepository_shouldThrowIllegalArgumentException(){
        assertThrows(
            IllegalArgumentException.class,
            () -> new TelemetryIngestionService(null, patientRepository, telemetryRepository)
        );
    }

    @Test
    void constructor_nullPatientRepository_shouldThrowIllegalArgumentException(){
        assertThrows(
            IllegalArgumentException.class,
            () -> new TelemetryIngestionService(deviceRepository, null, telemetryRepository)
        );
    }

    @Test
    void constructor_nullTelemetryRepository_shouldThrowIllegalArgumentException(){
        assertThrows(
            IllegalArgumentException.class,
            () -> new TelemetryIngestionService(deviceRepository, patientRepository, null)
        );
    }

    @Test
    void ingest_validTelemetry_shouldSaveEventAndUpdateDeviceLastSeenAt(){
        Patient patient = savePatient("PAT-001");
        MonitoringDevice device = saveAssignedDevice("DEV-001", patient.getId());
        Instant measuredAt = Instant.parse("2026-08-28T10:00:00Z");

        TelemetryEvent event = telemetryIngestionService.ingest(
            patient.getId(),
            device.getId(),
            MeasurementType.HEART_RATE,
            75.0,
            measuredAt
        );

        assertNotNull(event);
        assertEquals(1L, event.getId());
        assertEquals(patient.getId(), event.getPatientId());
        assertEquals(device.getId(), event.getDeviceId());
        assertEquals(MeasurementType.HEART_RATE, event.getType());
        assertEquals(75.0, event.getValue());
        assertEquals(measuredAt, event.getMeasuredAt());
        assertNotNull(event.getReceivedAt());
        assertEquals(event.getReceivedAt(), device.getLastSeenAt());
        assertEquals(1, telemetryRepository.count());
    }

    @Test
    void ingest_missingPatient_shouldThrowPatientNotFoundException(){
        MonitoringDevice device = saveAssignedDevice("DEV-001", 1L);

        assertThrows(
            PatientNotFoundException.class,
            () -> telemetryIngestionService.ingest(
                1L,
                device.getId(),
                MeasurementType.HEART_RATE,
                75.0,
                Instant.parse("2026-08-28T10:00:00Z")
            )
        );
    }

    @Test
    void ingest_inactivePatient_shouldThrowInvalidTelemetryException(){
        Patient patient = savePatient("PAT-001");
        MonitoringDevice device = saveAssignedDevice("DEV-001", patient.getId());
        patient.deactivate();

        assertThrows(
            InvalidTelemetryException.class,
            () -> telemetryIngestionService.ingest(
                patient.getId(),
                device.getId(),
                MeasurementType.HEART_RATE,
                75.0,
                Instant.parse("2026-08-28T10:00:00Z")
            )
        );
    }

    @Test
    void ingest_missingDevice_shouldThrowDeviceNotFoundException(){
        Patient patient = savePatient("PAT-001");

        assertThrows(
            DeviceNotFoundException.class,
            () -> telemetryIngestionService.ingest(
                patient.getId(),
                1L,
                MeasurementType.HEART_RATE,
                75.0,
                Instant.parse("2026-08-28T10:00:00Z")
            )
        );
    }

    @Test
    void ingest_inactiveDevice_shouldThrowInvalidTelemetryException(){
        Patient patient = savePatient("PAT-001");
        MonitoringDevice device = saveAssignedDevice("DEV-001", patient.getId());
        device.deactivate();

        assertThrows(
            InvalidTelemetryException.class,
            () -> telemetryIngestionService.ingest(
                patient.getId(),
                device.getId(),
                MeasurementType.HEART_RATE,
                75.0,
                Instant.parse("2026-08-28T10:00:00Z")
            )
        );
    }

    @Test
    void ingest_maintenanceDevice_shouldThrowInvalidTelemetryException(){
        Patient patient = savePatient("PAT-001");
        MonitoringDevice device = saveAssignedDevice("DEV-001", patient.getId());
        device.sendToMaintenance();

        assertThrows(
            InvalidTelemetryException.class,
            () -> telemetryIngestionService.ingest(
                patient.getId(),
                device.getId(),
                MeasurementType.HEART_RATE,
                75.0,
                Instant.parse("2026-08-28T10:00:00Z")
            )
        );
    }

    @Test
    void ingest_unassignedDevice_shouldThrowDeviceNotAssignedException(){
        Patient patient = savePatient("PAT-001");
        MonitoringDevice device = saveDevice("DEV-001");

        assertThrows(
            DeviceNotAssignedException.class,
            () -> telemetryIngestionService.ingest(
                patient.getId(),
                device.getId(),
                MeasurementType.HEART_RATE,
                75.0,
                Instant.parse("2026-08-28T10:00:00Z")
            )
        );
    }

    @Test
    void ingest_deviceAssignedToAnotherPatient_shouldThrowInvalidTelemetryException(){
        Patient firstPatient = savePatient("PAT-001");
        Patient secondPatient = savePatient("PAT-002");
        MonitoringDevice device = saveAssignedDevice("DEV-001", firstPatient.getId());

        assertThrows(
            InvalidTelemetryException.class,
            () -> telemetryIngestionService.ingest(
                secondPatient.getId(),
                device.getId(),
                MeasurementType.HEART_RATE,
                75.0,
                Instant.parse("2026-08-28T10:00:00Z")
            )
        );
    }

    @Test
    void ingest_nullMeasurementType_shouldThrowIllegalArgumentException(){
        Patient patient = savePatient("PAT-001");
        MonitoringDevice device = saveAssignedDevice("DEV-001", patient.getId());

        assertThrows(
            IllegalArgumentException.class,
            () -> telemetryIngestionService.ingest(
                patient.getId(),
                device.getId(),
                null,
                75.0,
                Instant.parse("2026-08-28T10:00:00Z")
            )
        );
    }

    @Test
    void ingest_nonFiniteValue_shouldThrowIllegalArgumentException(){
        Patient patient = savePatient("PAT-001");
        MonitoringDevice device = saveAssignedDevice("DEV-001", patient.getId());

        assertThrows(
            IllegalArgumentException.class,
            () -> telemetryIngestionService.ingest(
                patient.getId(),
                device.getId(),
                MeasurementType.HEART_RATE,
                Double.NaN,
                Instant.parse("2026-08-28T10:00:00Z")
            )
        );
    }

    @Test
    void ingest_nullMeasuredAt_shouldThrowIllegalArgumentException(){
        Patient patient = savePatient("PAT-001");
        MonitoringDevice device = saveAssignedDevice("DEV-001", patient.getId());

        assertThrows(
            IllegalArgumentException.class,
            () -> telemetryIngestionService.ingest(
                patient.getId(),
                device.getId(),
                MeasurementType.HEART_RATE,
                75.0,
                null
            )
        );
    }

    @Test
    void ingest_savedEvent_shouldBeAvailableAsLatestTelemetry(){
        Patient patient = savePatient("PAT-001");
        MonitoringDevice device = saveAssignedDevice("DEV-001", patient.getId());
        Instant measuredAt = Instant.parse("2026-08-28T10:00:00Z");

        TelemetryEvent event = telemetryIngestionService.ingest(
            patient.getId(),
            device.getId(),
            MeasurementType.OXYGEN_SATURATION,
            98.0,
            measuredAt
        );

        TelemetryEvent latestEvent = telemetryRepository.findLatestByPatientIdAndType(
            patient.getId(),
            MeasurementType.OXYGEN_SATURATION
        ).orElseThrow();

        assertSame(event, latestEvent);
    }

    @Test
    void ingest_invalidTelemetry_shouldNotSaveEventOrUpdateDeviceLastSeenAt(){
        Patient patient = savePatient("PAT-001");
        MonitoringDevice device = saveAssignedDevice("DEV-001", patient.getId());

        assertThrows(
            IllegalArgumentException.class,
            () -> telemetryIngestionService.ingest(
                patient.getId(),
                device.getId(),
                MeasurementType.HEART_RATE,
                Double.POSITIVE_INFINITY,
                Instant.parse("2026-08-28T10:00:00Z")
            )
        );

        assertEquals(0, telemetryRepository.count());
        assertNull(device.getLastSeenAt());
    }

    private MonitoringDevice saveAssignedDevice(String deviceCode, Long patientId){
        MonitoringDevice device = saveDevice(deviceCode);
        device.assignToPatient(patientId);
        return device;
    }

    private MonitoringDevice saveDevice(String deviceCode){
        MonitoringDevice device = new MonitoringDevice(
            deviceCode,
            DeviceType.MULTIPARAMETER_MONITOR,
            Instant.parse("2026-08-28T09:00:00Z")
        );

        assertEquals(DeviceStatus.ACTIVE, device.getStatus());

        return deviceRepository.save(device);
    }

    private Patient savePatient(String patientCode){
        Patient patient = new Patient(
            patientCode,
            "ICU-A",
            "BED-01",
            Instant.parse("2026-08-28T08:00:00Z")
        );

        return patientRepository.save(patient);
    }
}
