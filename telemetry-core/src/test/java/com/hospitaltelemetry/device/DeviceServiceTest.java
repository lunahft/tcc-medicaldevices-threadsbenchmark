package com.hospitaltelemetry.device;

import com.hospitaltelemetry.device.exception.DeviceNotAssignedException;
import com.hospitaltelemetry.device.exception.DeviceNotFoundException;
import com.hospitaltelemetry.device.exception.DuplicateDeviceCodeException;
import com.hospitaltelemetry.device.exception.InvalidDeviceStateException;
import com.hospitaltelemetry.patient.InMemoryPatientRepository;
import com.hospitaltelemetry.patient.Patient;
import com.hospitaltelemetry.patient.PatientRepository;
import com.hospitaltelemetry.patient.exception.PatientNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DeviceServiceTest {
    private DeviceService deviceService;
    private PatientRepository patientRepository;

    @BeforeEach
    void setUp(){
        patientRepository = new InMemoryPatientRepository();
        deviceService = new DeviceService(new InMemoryDeviceRepository(), patientRepository);
    }

    @Test
    void registerDevice_validData_shouldRegisterDevice(){
        Instant registeredAt = Instant.parse("2026-08-28T10:00:00Z");

        MonitoringDevice device = deviceService.registerDevice(
            "DEV-001",
            DeviceType.MULTIPARAMETER_MONITOR,
            registeredAt
        );

        assertNotNull(device);
        assertEquals(1L, device.getId());
        assertEquals("DEV-001", device.getDeviceCode());
        assertEquals(DeviceType.MULTIPARAMETER_MONITOR, device.getType());
        assertEquals(DeviceStatus.ACTIVE, device.getStatus());
        assertEquals(registeredAt, device.getRegisteredAt());
        assertNull(device.getPatientId());
        assertNull(device.getLastSeenAt());
    }

    @Test
    void registerDevice_duplicateCode_shouldThrowDuplicateDeviceCodeException(){
        Instant registeredAt = Instant.parse("2026-08-28T10:00:00Z");
        deviceService.registerDevice(
            "DEV-001",
            DeviceType.MULTIPARAMETER_MONITOR,
            registeredAt
        );

        assertThrows(
            DuplicateDeviceCodeException.class,
            () -> deviceService.registerDevice(
                "DEV-001",
                DeviceType.PULSE_OXIMETER,
                registeredAt.plusSeconds(60)
            )
        );
    }

    @Test
    void findById_existingDevice_shouldReturnDevice(){
        Instant registeredAt = Instant.parse("2026-08-28T10:00:00Z");
        MonitoringDevice device = deviceService.registerDevice(
            "DEV-001",
            DeviceType.MULTIPARAMETER_MONITOR,
            registeredAt
        );

        MonitoringDevice foundDevice = deviceService.findById(1L);

        assertNotNull(foundDevice);
        assertEquals(device.getId(), foundDevice.getId());
        assertEquals(device.getDeviceCode(), foundDevice.getDeviceCode());
        assertEquals(device.getType(), foundDevice.getType());
        assertEquals(device.getStatus(), foundDevice.getStatus());
        assertEquals(device.getPatientId(), foundDevice.getPatientId());
        assertEquals(device.getRegisteredAt(), foundDevice.getRegisteredAt());
        assertEquals(device.getLastSeenAt(), foundDevice.getLastSeenAt());
    }

    @Test
    void findById_missingDevice_shouldThrowDeviceNotFoundException(){
        assertThrows(DeviceNotFoundException.class, () -> deviceService.findById(2L));
    }

    @Test
    void findByCode_existingDevice_shouldReturnDevice(){
        Instant registeredAt = Instant.parse("2026-08-28T10:00:00Z");
        MonitoringDevice device = deviceService.registerDevice(
            "DEV-001",
            DeviceType.MULTIPARAMETER_MONITOR,
            registeredAt
        );

        MonitoringDevice foundDevice = deviceService.findByCode("DEV-001");

        assertNotNull(foundDevice);
        assertEquals(device.getId(), foundDevice.getId());
        assertEquals(device.getDeviceCode(), foundDevice.getDeviceCode());
        assertEquals(device.getType(), foundDevice.getType());
        assertEquals(device.getStatus(), foundDevice.getStatus());
        assertEquals(device.getPatientId(), foundDevice.getPatientId());
        assertEquals(device.getRegisteredAt(), foundDevice.getRegisteredAt());
        assertEquals(device.getLastSeenAt(), foundDevice.getLastSeenAt());
    }

    @Test
    void findByCode_missingDevice_shouldThrowDeviceNotFoundException(){
        assertThrows(DeviceNotFoundException.class, () -> deviceService.findByCode("DEV-001"));
    }

    @Test
    void findAll_savedDevices_shouldReturnDevices(){
        Instant registeredAt = Instant.parse("2026-08-28T10:00:00Z");
        MonitoringDevice firstDevice = deviceService.registerDevice(
            "DEV-001",
            DeviceType.MULTIPARAMETER_MONITOR,
            registeredAt
        );
        MonitoringDevice secondDevice = deviceService.registerDevice(
            "DEV-002",
            DeviceType.PULSE_OXIMETER,
            registeredAt.plusSeconds(60)
        );

        List<MonitoringDevice> devices = deviceService.findAll();

        assertEquals(2, devices.size());
        assertTrue(devices.contains(firstDevice));
        assertTrue(devices.contains(secondDevice));
    }

    @Test
    void assignDeviceToPatient_activeDeviceAndActivePatient_shouldAssignDevice(){
        MonitoringDevice device = registerDevice("DEV-001");
        Patient patient = savePatient("PAT-001");

        MonitoringDevice assignedDevice = deviceService.assignDeviceToPatient(
            device.getId(),
            patient.getId()
        );

        assertTrue(assignedDevice.isAssigned());
        assertEquals(patient.getId(), assignedDevice.getPatientId());
    }

    @Test
    void assignDeviceToPatient_missingDevice_shouldThrowDeviceNotFoundException(){
        Patient patient = savePatient("PAT-001");

        assertThrows(
            DeviceNotFoundException.class,
            () -> deviceService.assignDeviceToPatient(2L, patient.getId())
        );
    }

    @Test
    void assignDeviceToPatient_missingPatient_shouldThrowPatientNotFoundException(){
        MonitoringDevice device = registerDevice("DEV-001");

        assertThrows(
            PatientNotFoundException.class,
            () -> deviceService.assignDeviceToPatient(device.getId(), 2L)
        );
    }

    @Test
    void assignDeviceToPatient_inactivePatient_shouldThrowInvalidDeviceStateException(){
        MonitoringDevice device = registerDevice("DEV-001");
        Patient patient = savePatient("PAT-001");
        patient.deactivate();

        assertThrows(
            InvalidDeviceStateException.class,
            () -> deviceService.assignDeviceToPatient(device.getId(), patient.getId())
        );
    }

    @Test
    void assignDeviceToPatient_inactiveDevice_shouldThrowInvalidDeviceStateException(){
        MonitoringDevice device = registerDevice("DEV-001");
        Patient patient = savePatient("PAT-001");
        device.deactivate();

        assertThrows(
            InvalidDeviceStateException.class,
            () -> deviceService.assignDeviceToPatient(device.getId(), patient.getId())
        );
    }

    @Test
    void assignDeviceToPatient_assignedDevice_shouldThrowInvalidDeviceStateException(){
        MonitoringDevice device = registerDevice("DEV-001");
        Patient firstPatient = savePatient("PAT-001");
        Patient secondPatient = savePatient("PAT-002");
        deviceService.assignDeviceToPatient(device.getId(), firstPatient.getId());

        assertThrows(
            InvalidDeviceStateException.class,
            () -> deviceService.assignDeviceToPatient(device.getId(), secondPatient.getId())
        );
    }

    @Test
    void unassignDevice_assignedDevice_shouldUnassignDevice(){
        MonitoringDevice device = registerDevice("DEV-001");
        Patient patient = savePatient("PAT-001");
        deviceService.assignDeviceToPatient(device.getId(), patient.getId());

        MonitoringDevice unassignedDevice = deviceService.unassignDevice(
            device.getId(),
            patient.getId()
        );

        assertFalse(unassignedDevice.isAssigned());
        assertNull(unassignedDevice.getPatientId());
    }

    @Test
    void unassignDevice_missingDevice_shouldThrowDeviceNotFoundException(){
        assertThrows(
            DeviceNotFoundException.class,
            () -> deviceService.unassignDevice(2L, 1L)
        );
    }

    @Test
    void unassignDevice_unassignedDevice_shouldThrowDeviceNotAssignedException(){
        MonitoringDevice device = registerDevice("DEV-001");

        assertThrows(
            DeviceNotAssignedException.class,
            () -> deviceService.unassignDevice(device.getId(), 1L)
        );
    }

    @Test
    void sendToMaintenance_activeDevice_shouldChangeStatusToMaintenance(){
        MonitoringDevice device = registerDevice("DEV-001");

        deviceService.sendToMaintenance(device.getId());

        assertEquals(DeviceStatus.MAINTENANCE, deviceService.findById(device.getId()).getStatus());
    }

    @Test
    void sendToMaintenance_missingDevice_shouldThrowDeviceNotFoundException(){
        assertThrows(DeviceNotFoundException.class, () -> deviceService.sendToMaintenance(2L));
    }

    @Test
    void activateDevice_maintenanceDevice_shouldChangeStatusToActive(){
        MonitoringDevice device = registerDevice("DEV-001");
        deviceService.sendToMaintenance(device.getId());

        deviceService.activateDevice(device.getId());

        assertEquals(DeviceStatus.ACTIVE, deviceService.findById(device.getId()).getStatus());
    }

    @Test
    void activateDevice_missingDevice_shouldThrowDeviceNotFoundException(){
        assertThrows(DeviceNotFoundException.class, () -> deviceService.activateDevice(2L));
    }

    @Test
    void deactivateDevice_activeDevice_shouldChangeStatusToInactive(){
        MonitoringDevice device = registerDevice("DEV-001");

        deviceService.deactivateDevice(device.getId());

        assertEquals(DeviceStatus.INACTIVE, deviceService.findById(device.getId()).getStatus());
    }

    @Test
    void deactivateDevice_missingDevice_shouldThrowDeviceNotFoundException(){
        assertThrows(DeviceNotFoundException.class, () -> deviceService.deactivateDevice(2L));
    }

    private MonitoringDevice registerDevice(String deviceCode){
        return deviceService.registerDevice(
            deviceCode,
            DeviceType.MULTIPARAMETER_MONITOR,
            Instant.parse("2026-08-28T10:00:00Z")
        );
    }

    private Patient savePatient(String patientCode){
        Patient patient = new Patient(
            patientCode,
            "ICU-A",
            "BED-01",
            Instant.parse("2026-08-28T10:00:00Z")
        );

        return patientRepository.save(patient);
    }
}
