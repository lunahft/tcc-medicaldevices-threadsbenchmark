package com.hospitaltelemetry.api.telemetry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.hospitaltelemetry.device.DeviceService;
import com.hospitaltelemetry.device.DeviceType;
import com.hospitaltelemetry.device.InMemoryDeviceRepository;
import com.hospitaltelemetry.patient.InMemoryPatientRepository;
import com.hospitaltelemetry.patient.Patient;
import com.hospitaltelemetry.telemetry.InMemoryTelemetryRepository;
import com.hospitaltelemetry.telemetry.MeasurementType;
import com.hospitaltelemetry.telemetry.TelemetryIngestionService;
import com.hospitaltelemetry.telemetry.TelemetryQueryService;

class TelemetryControllerTest {

    private static final Instant REGISTERED_AT = Instant.parse("2026-09-12T00:00:00Z");
    private static final Instant MEASURED_AT = Instant.parse("2026-09-12T01:00:00Z");
    private static final String VALID_REQUEST = """
            {
              "patientId": 1,
              "deviceId": 1,
              "type": "HEART_RATE",
              "value": 72.5,
              "measuredAt": "2026-09-12T01:00:00Z"
            }
            """;

    private MockMvc mockMvc;
    private InMemoryTelemetryRepository telemetryRepository;
    private InMemoryPatientRepository patientRepository;
    private DeviceService deviceService;
    private TelemetryIngestionService ingestionService;

    @BeforeEach
    void setUp() {
        patientRepository = new InMemoryPatientRepository();
        var deviceRepository = new InMemoryDeviceRepository();
        telemetryRepository = new InMemoryTelemetryRepository();
        deviceService = new DeviceService(deviceRepository, patientRepository);
        ingestionService = new TelemetryIngestionService(
                deviceRepository, patientRepository, telemetryRepository);
        var queryService = new TelemetryQueryService(telemetryRepository, patientRepository);

        patientRepository.save(new Patient("PAT-001", "ICU", "BED-01", REGISTERED_AT));
        deviceService.registerDevice("DEV-001", DeviceType.MULTIPARAMETER_MONITOR, REGISTERED_AT);
        deviceService.assignDeviceToPatient(1L, 1L);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new TelemetryController(ingestionService, queryService))
                .setControllerAdvice(new TelemetryExceptionHandler())
                .build();
    }

    @Test
    void shouldIngestTelemetryAndReturnDerivedUnit() throws Exception {
        mockMvc.perform(post("/telemetry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_REQUEST))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.patientId").value(1))
                .andExpect(jsonPath("$.deviceId").value(1))
                .andExpect(jsonPath("$.type").value("HEART_RATE"))
                .andExpect(jsonPath("$.unit").value("BPM"))
                .andExpect(jsonPath("$.value").value(72.5))
                .andExpect(jsonPath("$.measuredAt").exists())
                .andExpect(jsonPath("$.receivedAt").exists());

        assertEquals(1, telemetryRepository.count());
        var saved = telemetryRepository.findLatestByPatientIdAndType(1L, MeasurementType.HEART_RATE)
                .orElseThrow();
        assertEquals(MEASURED_AT, saved.getMeasuredAt());
        assertFalse(saved.getReceivedAt().isBefore(MEASURED_AT));
        assertEquals(saved.getReceivedAt(), deviceService.findById(1L).getLastSeenAt());
    }

    @ParameterizedTest
    @ValueSource(strings = {"patientId", "deviceId", "type", "value", "measuredAt"})
    void shouldRejectMissingRequiredField(String field) throws Exception {
        String request = VALID_REQUEST.replaceAll("\\s*\"" + field + "\": [^,\\n]+,?", "")
                .replaceAll(",\\s*}", "}");

        mockMvc.perform(post("/telemetry").contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isBadRequest());

        assertEquals(0, telemetryRepository.count());
    }

    @ParameterizedTest
    @ValueSource(strings = {"\"patientId\": 0", "\"deviceId\": -1", "\"type\": \"UNKNOWN\"",
            "\"value\": null", "\"value\": 1e400", "\"measuredAt\": \"invalid\"",
            "\"measuredAt\": \"2999-01-01T00:00:00Z\""})
    void shouldRejectInvalidTelemetry(String replacement) throws Exception {
        String field = replacement.substring(0, replacement.indexOf(':'));
        String request = VALID_REQUEST.replaceAll(field + ": [^,\\n]+", replacement);

        mockMvc.perform(post("/telemetry").contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isBadRequest());

        assertEquals(0, telemetryRepository.count());
    }

    @ParameterizedTest
    @ValueSource(strings = {"patientId", "deviceId"})
    void shouldReturnNotFoundForUnknownPatientOrDevice(String field) throws Exception {
        String request = VALID_REQUEST.replace("\"" + field + "\": 1", "\"" + field + "\": 999");

        mockMvc.perform(post("/telemetry").contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").exists());

        assertEquals(0, telemetryRepository.count());
    }

    @ParameterizedTest
    @ValueSource(strings = {"inactivePatient", "inactiveDevice", "unassignedDevice", "differentPatient"})
    void shouldReturnConflictForInvalidMonitoringState(String state) throws Exception {
        switch (state) {
            case "inactivePatient" -> patientRepository.findById(1L).orElseThrow().discharge(MEASURED_AT);
            case "inactiveDevice" -> deviceService.deactivateDevice(1L);
            case "unassignedDevice" -> deviceService.unassignDevice(1L, 1L);
            case "differentPatient" -> {
                patientRepository.save(new Patient("PAT-002", "ICU", "BED-02", REGISTERED_AT));
                deviceService.unassignDevice(1L, 1L);
                deviceService.assignDeviceToPatient(1L, 2L);
            }
            default -> throw new IllegalArgumentException(state);
        }

        mockMvc.perform(post("/telemetry").contentType(MediaType.APPLICATION_JSON).content(VALID_REQUEST))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").exists());

        assertEquals(0, telemetryRepository.count());
    }

    @Test
    void shouldReturnLatestMeasurementForRequestedType() throws Exception {
        ingestionService.ingest(1L, 1L, MeasurementType.HEART_RATE, 70, REGISTERED_AT);
        ingestionService.ingest(1L, 1L, MeasurementType.HEART_RATE, 72.5, MEASURED_AT);
        ingestionService.ingest(1L, 1L, MeasurementType.BODY_TEMPERATURE, 36.5, MEASURED_AT);

        mockMvc.perform(get("/telemetry/latest").param("patientId", "1").param("type", "HEART_RATE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.value").value(72.5))
                .andExpect(jsonPath("$.unit").value("BPM"));
    }

    @Test
    void shouldReturnNoContentWhenMeasurementDoesNotExist() throws Exception {
        mockMvc.perform(get("/telemetry/latest").param("patientId", "1").param("type", "HEART_RATE"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnHistoryWithinRequestedPeriod() throws Exception {
        ingestionService.ingest(1L, 1L, MeasurementType.HEART_RATE, 70, REGISTERED_AT);
        ingestionService.ingest(1L, 1L, MeasurementType.BODY_TEMPERATURE, 36.5, MEASURED_AT);

        mockMvc.perform(get("/telemetry/history").param("patientId", "1")
                        .param("start", MEASURED_AT.toString()).param("end", MEASURED_AT.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].value").value(36.5))
                .andExpect(jsonPath("$[0].unit").value("CELSIUS"));
    }

    @Test
    void shouldReturnEmptyHistoryWhenNoEventsExist() throws Exception {
        mockMvc.perform(get("/telemetry/history").param("patientId", "1")
                        .param("start", REGISTERED_AT.toString()).param("end", MEASURED_AT.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/telemetry/latest", "/telemetry/history"})
    void shouldReturnNotFoundWhenQueryingUnknownPatient(String path) throws Exception {
        mockMvc.perform(get(path).param("patientId", "999").param("type", "HEART_RATE")
                        .param("start", REGISTERED_AT.toString()).param("end", MEASURED_AT.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectReversedHistoryPeriod() throws Exception {
        mockMvc.perform(get("/telemetry/history").param("patientId", "1")
                        .param("start", MEASURED_AT.toString()).param("end", REGISTERED_AT.toString()))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/telemetry/latest?patientId=1",
            "/telemetry/latest?type=HEART_RATE",
            "/telemetry/latest?patientId=1&type=UNKNOWN",
            "/telemetry/history?patientId=1&start=invalid&end=2026-09-12T01:00:00Z",
            "/telemetry/history?patientId=1&start=2026-09-12T00:00:00Z"
    })
    void shouldRejectInvalidQueryParameters(String path) throws Exception {
        mockMvc.perform(get(path)).andExpect(status().isBadRequest());
    }
}
