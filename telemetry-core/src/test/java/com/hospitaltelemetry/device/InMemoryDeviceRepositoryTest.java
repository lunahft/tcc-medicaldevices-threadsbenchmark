package com.hospitaltelemetry.device;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryDeviceRepositoryTest {
    private InMemoryDeviceRepository repository;

    @BeforeEach
    void setUp(){
        repository = new InMemoryDeviceRepository();
    }

    @Test
    void save_validDevice_shouldAssignIdAndReturnDevice(){
        MonitoringDevice device = createDevice("DEV-001");
        MonitoringDevice savedDevice = repository.save(device);
        assertSame(device, savedDevice);
        assertEquals(1L, savedDevice.getId());
    }

    @Test
    void save_multipleDevices_shouldGenerateSequentialIds(){
        MonitoringDevice firstDevice = createDevice("DEV-001");
        MonitoringDevice secondDevice = createDevice("DEV-002");

        repository.save(firstDevice);
        repository.save(secondDevice);

        assertEquals(1L, firstDevice.getId());
        assertEquals(2L, secondDevice.getId());
    }

    @Test
    void findById_existingDevice_shouldReturnDevice(){
        MonitoringDevice device = repository.save(createDevice("DEV-001"));
        Optional<MonitoringDevice> foundDevice = repository.findById(1L);
        assertTrue(foundDevice.isPresent());
        assertSame(device, foundDevice.get());
    }

    @Test
    void findById_missingDevice_shouldReturnEmptyOptional(){
        Optional<MonitoringDevice> foundDevice = repository.findById(99L);
        assertTrue(foundDevice.isEmpty());
    }

    @Test
    void findByDeviceCode_existingDeviceCode_shouldReturnDevice(){
        MonitoringDevice device = repository.save(createDevice("DEV-001"));
        Optional<MonitoringDevice> foundDevice = repository.findByDeviceCode("DEV-001");
        assertTrue(foundDevice.isPresent());
        assertSame(device, foundDevice.get());
    }

    @Test
    void findByDeviceCode_missingDeviceCode_shouldReturnEmptyOptional(){
        repository.save(createDevice("DEV-001"));
        Optional<MonitoringDevice> foundDevice = repository.findByDeviceCode("DEV-999");
        assertTrue(foundDevice.isEmpty());
    }

    @Test
    void existsByDeviceCode_existingDeviceCode_shouldReturnTrue(){
        repository.save(createDevice("DEV-001"));
        boolean exists = repository.existsByDeviceCode("DEV-001");
        assertTrue(exists);
    }

    @Test
    void existsByDeviceCode_missingDeviceCode_shouldReturnFalse(){
        boolean exists = repository.existsByDeviceCode("DEV-999");
        assertFalse(exists);
    }

    @Test
    void findAll_savedDevices_shouldReturnAllDevices(){
        MonitoringDevice firstDevice = repository.save(createDevice("DEV-001"));
        MonitoringDevice secondDevice = repository.save(createDevice("DEV-002"));

        List<MonitoringDevice> devices = repository.findAll();

        assertEquals(2, devices.size());
        assertTrue(devices.contains(firstDevice));
        assertTrue(devices.contains(secondDevice));
    }

    @Test
    void findAll_noDevices_shouldReturnEmptyList(){
        List<MonitoringDevice> devices = repository.findAll();
        assertTrue(devices.isEmpty());
    }

    @Test
    void save_nullDevice_shouldThrowIllegalArgumentException(){
        assertThrows(
            IllegalArgumentException.class,
            () -> repository.save(null)
        );
    }

    private static MonitoringDevice createDevice(String deviceCode){
        return new MonitoringDevice(
            deviceCode,
            DeviceType.MULTIPARAMETER_MONITOR,
            Instant.parse("2026-08-28T10:00:00Z")
        );
    }
}