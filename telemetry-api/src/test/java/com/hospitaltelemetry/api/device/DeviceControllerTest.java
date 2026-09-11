package com.hospitaltelemetry.api.device;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.hospitaltelemetry.device.DeviceService;
import com.hospitaltelemetry.device.InMemoryDeviceRepository;
import com.hospitaltelemetry.device.MonitoringDevice;
import com.hospitaltelemetry.patient.InMemoryPatientRepository;
import com.hospitaltelemetry.patient.Patient;
import com.hospitaltelemetry.patient.PatientRepository;

class DeviceControllerTest {

    private static final Instant REGISTERED_AT = Instant.parse("2026-09-12T00:00:00Z");

    private MockMvc mockMvc;
    private DeviceService deviceService;
    private PatientRepository patientRepository;

    @BeforeEach
    void setUp() {
        patientRepository = new InMemoryPatientRepository();
        deviceService = new DeviceService(new InMemoryDeviceRepository(), patientRepository);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new DeviceController(deviceService))
                .setControllerAdvice(new DeviceExceptionHandler())
                .build();
    }

    @Test
    void shouldRegisterDevice() throws Exception {
        mockMvc.perform(post("/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deviceCode": "DEV-001",
                                  "type": "MULTIPARAMETER_MONITOR",
                                  "registeredAt": "2026-09-12T00:00:00Z"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/devices/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.deviceCode").value("DEV-001"))
                .andExpect(jsonPath("$.type").value("MULTIPARAMETER_MONITOR"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldFindDevicesByIdAndCodeAndListAll() throws Exception {
        registerDevice("DEV-001");

        mockMvc.perform(get("/devices/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviceCode").value("DEV-001"));

        mockMvc.perform(get("/devices/by-code").param("deviceCode", "DEV-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        mockMvc.perform(get("/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].deviceCode").value("DEV-001"));
    }

    @Test
    void shouldAssignAndUnassignDevice() throws Exception {
        MonitoringDevice device = registerDevice("DEV-001");
        Patient patient = patientRepository.save(new Patient(
                "PAT-001",
                "ICU",
                "BED-01",
                REGISTERED_AT
        ));

        mockMvc.perform(patch("/devices/{id}/assign", device.getId())
                        .param("patientId", patient.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value(patient.getId()));

        mockMvc.perform(patch("/devices/{id}/unassign", device.getId())
                        .param("patientId", patient.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.patientId").value(nullValue()));
    }

    @Test
    void shouldChangeDeviceStatus() throws Exception {
        MonitoringDevice device = registerDevice("DEV-001");

        mockMvc.perform(patch("/devices/{id}/maintenance", device.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("MAINTENANCE"));

        mockMvc.perform(patch("/devices/{id}/activate", device.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        mockMvc.perform(patch("/devices/{id}/deactivate", device.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    void shouldReturnNotFoundWhenDeviceDoesNotExist() throws Exception {
        mockMvc.perform(get("/devices/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Device was not found with id: 999"));
    }

    @Test
    void shouldReturnConflictWhenDeviceCodeAlreadyExists() throws Exception {
        registerDevice("DEV-001");

        mockMvc.perform(post("/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deviceCode": "DEV-001",
                                  "type": "THERMOMETER",
                                  "registeredAt": "2026-09-12T00:00:00Z"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail")
                        .value("Device already exists containing code: DEV-001"));
    }

    private MonitoringDevice registerDevice(String deviceCode) {
        return deviceService.registerDevice(
                deviceCode,
                com.hospitaltelemetry.device.DeviceType.MULTIPARAMETER_MONITOR,
                REGISTERED_AT
        );
    }
}
