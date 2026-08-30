package com.hospitaltelemetry.patient;

import com.hospitaltelemetry.patient.exception.DuplicatePatientCodeException;
import com.hospitaltelemetry.patient.exception.PatientNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PatientServiceTest {
    private PatientService patientService;

    @BeforeEach
    void setUp(){
        patientService = new PatientService(new InMemoryPatientRepository());
    }

    @Test
    void registerPatient_validData_shouldRegisterPatient(){
        Instant admittedAt = Instant.parse("2026-08-28T10:00:00Z");

        Patient patient = patientService.registerPatient(
            "PAT-001",
            "ICU-A",
            "BED-01",
            admittedAt
        );

        assertNotNull(patient);
        assertEquals(1L, patient.getId());
        assertEquals("PAT-001", patient.getPatientCode());
        assertEquals("ICU-A", patient.getUnitCode());
        assertEquals("BED-01", patient.getBedCode());
        assertEquals(PatientStatus.ACTIVE, patient.getStatus());
        assertEquals(admittedAt, patient.getAdmittedAt());
        assertNull(patient.getDischargedAt());
    }

    @Test
    void registerPatient_duplicateCode_shouldThrowDuplicatePatientCodeException(){
        Instant admittedAt = Instant.parse("2026-08-28T10:00:00Z");
        patientService.registerPatient(
            "PAT-001",
            "ICU-A",
            "BED-01",
            admittedAt
        );

        assertThrows(
            DuplicatePatientCodeException.class,
            () -> patientService.registerPatient(
                "PAT-001",
                "ICU-B",
                "BED-02",
                admittedAt.plusSeconds(60)
            )
        );
    }

    @Test
    void findById_existingPatient_shouldReturnPatient(){
        Instant admittedAt = Instant.parse("2026-08-28T10:00:00Z");
        Patient patient = patientService.registerPatient(
            "PAT-001",
            "ICU-A",
            "BED-01",
            admittedAt
        );

        Patient foundPatient = patientService.findById(1L);

        assertNotNull(foundPatient);
        assertEquals(patient.getId(), foundPatient.getId());
        assertEquals(patient.getPatientCode(), foundPatient.getPatientCode());
        assertEquals(patient.getUnitCode(), foundPatient.getUnitCode());
        assertEquals(patient.getBedCode(), foundPatient.getBedCode());
        assertEquals(patient.getStatus(), foundPatient.getStatus());
        assertEquals(patient.getAdmittedAt(), foundPatient.getAdmittedAt());
        assertEquals(patient.getDischargedAt(), foundPatient.getDischargedAt());
    }

    @Test
    void findById_missingPatient_shouldThrowPatientNotFoundException(){
        assertThrows(PatientNotFoundException.class, () -> patientService.findById(2L));
    }

    @Test
    void findByPatientCode_existingPatient_shouldReturnPatient(){
        Instant admittedAt = Instant.now();
        Patient patient = patientService.registerPatient(
            "PAT-001",
            "ICU-A",
            "BED-01",
            admittedAt
        );

        Patient foundPatient = patientService.findByPatientCode("PAT-001");

        assertNotNull(foundPatient);
        assertEquals(patient.getId(), foundPatient.getId());
        assertEquals(patient.getPatientCode(), foundPatient.getPatientCode());
        assertEquals(patient.getUnitCode(), foundPatient.getUnitCode());
        assertEquals(patient.getBedCode(), foundPatient.getBedCode());
        assertEquals(patient.getStatus(), foundPatient.getStatus());
        assertEquals(patient.getAdmittedAt(), foundPatient.getAdmittedAt());
        assertEquals(patient.getDischargedAt(), foundPatient.getDischargedAt());
    }

    @Test
    void findByPatientCode_missingPatient_shouldThrowPatientNotFoundException(){
        assertThrows(PatientNotFoundException.class, () -> patientService.findByPatientCode("PAT-001"));
    }

    @Test
    void findAll_savedPatients_shouldReturnPatients(){
        Instant admittedAt = Instant.now();
        Patient firstPatient = patientService.registerPatient(
            "PAT-001",
            "ICU-A",
            "BED-01",
            admittedAt
        );
        Patient secondPatient = patientService.registerPatient(
            "PAT-002",
            "ICU-B",
            "BED-02",
            admittedAt.plusSeconds(60)
        );

        List<Patient> patients = patientService.findAll();

        assertEquals(2, patients.size());
        assertTrue(patients.contains(firstPatient));
        assertTrue(patients.contains(secondPatient));
    }

    @Test
    void dischargePatient_activePatient_shouldDischarge(){
        Instant admittedAt = Instant.now();
        Patient patient = patientService.registerPatient(
            "PAT-001",
            "ICU-A",
            "BED-01",
            admittedAt
        );

        Long patientId = patient.getId();

        patientService.dischargePatient(patientId, Instant.now());
        assertEquals(PatientStatus.DISCHARGED, patientService.findById(patientId).getStatus());
    }

    @Test
    void dischargePatient_missingPatient_shouldThrowPatientNotFoundException(){
        Instant dischargedAt = Instant.now();
        assertThrows(PatientNotFoundException.class, () -> patientService.dischargePatient(265L, dischargedAt));
    }

    @Test
    void deactivatePatient_activePatient_shouldDeactivate(){
        Instant admittedAt = Instant.now();
        Patient patient = patientService.registerPatient(
            "PAT-001",
            "ICU-A",
            "BED-01",
            admittedAt
        );

        Long patientId = patient.getId();

        patientService.deactivatePatient(patientId);
        assertEquals(PatientStatus.INACTIVE, patientService.findById(patientId).getStatus());
    }

    @Test
    void deactivatePatient_missingPatient_shouldThrowPatientNotFoundException(){
        assertThrows(PatientNotFoundException.class, () -> patientService.deactivatePatient(6L));
    }
}
