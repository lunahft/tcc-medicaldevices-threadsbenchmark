package com.hospitaltelemetry.api.patient;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.hospitaltelemetry.patient.InMemoryPatientRepository;
import com.hospitaltelemetry.patient.PatientService;

class PatientControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        PatientService patientService = new PatientService(new InMemoryPatientRepository());

        mockMvc = MockMvcBuilders
                .standaloneSetup(new PatientController(patientService))
                .setControllerAdvice(new PatientExceptionHandler())
                .build();
    }

    @Test
    void shouldRegisterPatient() throws Exception {
        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "patientCode": "PAT-001",
                                  "unitCode": "ICU",
                                  "bedCode": "BED-01",
                                  "admittedAt": "2026-09-12T00:00:00Z"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/patients/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.patientCode").value("PAT-001"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldReturnNotFoundWhenPatientDoesNotExist() throws Exception {
        mockMvc.perform(get("/patients/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Patient not found with id: 999"));
    }
}
