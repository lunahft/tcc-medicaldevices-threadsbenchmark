package com.hospitaltelemetry.device;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.hospitaltelemetry.device.exception.DeviceNotAssignedException;

import java.time.Instant;


public class MonitoringDeviceTest {
    private MonitoringDevice monitoringDevice;
    private static final Long VALID_PATIENT_ID = 1L;
    private static final Long ANOTHER_VALID_PATIENT_ID = 2L;
    private static final Long INVALID_PATIENT_ID = 0L;
    private static final String VALID_DEVICE_CODE = "DEV-001";
    private static final DeviceType VALID_DEVICE_TYPE = DeviceType.MULTIPARAMETER_MONITOR;
    private static final Instant VALID_REGISTERED_AT = Instant.parse("2026-08-28T10:00:00Z");


    @BeforeEach
    void setUp(){
        monitoringDevice = new MonitoringDevice(VALID_DEVICE_CODE, VALID_DEVICE_TYPE, VALID_REGISTERED_AT);
    }

    @Test
    void assignToPatient_validPatientId_shouldAssignPatient(){
        monitoringDevice.assignToPatient(VALID_PATIENT_ID);

        assertTrue(monitoringDevice.isAssigned());
    }

    @Test
    void assignToPatient_nullPatientId_shouldThrowIllegalArgumentException(){

        assertThrows(
            IllegalArgumentException.class, () -> monitoringDevice.assignToPatient(null)
        );
    }
    @Test
    void assignToPatient_invalidPatientId_shouldThrowIllegalArgumentException(){

        assertThrows(
            IllegalArgumentException.class, () -> monitoringDevice.assignToPatient(INVALID_PATIENT_ID)
        );
    }
    @Test
    void unassignPatient_assignedDevice_shouldUnassignPatient(){
        monitoringDevice.assignToPatient(VALID_PATIENT_ID);
        monitoringDevice.unassignPatient();

        assertFalse(monitoringDevice.isAssigned());
    }
    @Test
    void unassignPatient_unassignedDevice_shouldThrowDeviceNotAssignedException(){

        assertThrows(
            DeviceNotAssignedException.class, () -> monitoringDevice.unassignPatient()
        );
    }
    @Test
    void markSeenAt_validLastSeenAt_shouldUpdateLastSeenAt(){
        Instant validLastSeenAt = Instant.parse("2026-08-28T10:05:00Z");
        monitoringDevice.markSeenAt(validLastSeenAt);

        assertEquals(validLastSeenAt, monitoringDevice.getLastSeenAt());
    }
    @Test
    void markSeenAt_lastSeenAtBeforeRegisteredAt_shouldThrowIllegalArgumentException(){
        Instant validLastSeenAt = Instant.parse("2026-08-28T10:05:00Z");
        monitoringDevice.markSeenAt(validLastSeenAt);
        Instant invalidLastSeenAt = Instant.parse("2026-08-28T09:55:00Z");

        assertThrows(
            IllegalArgumentException.class, () -> monitoringDevice.markSeenAt(invalidLastSeenAt)
        );
    }
    @Test 
    void assignId_alreadyHasId_shouldThrowIllegalStateException() {
        monitoringDevice.assignId(1L);

        assertThrows(
            IllegalStateException.class, () -> monitoringDevice.assignId(2L)
        );
    }
    @Test
    void assignId_invalidId__shouldThrowIllegalArgumentException(){
        
        assertThrows(
            IllegalArgumentException.class, () -> monitoringDevice.assignId(-1L)
        );
    }
}
