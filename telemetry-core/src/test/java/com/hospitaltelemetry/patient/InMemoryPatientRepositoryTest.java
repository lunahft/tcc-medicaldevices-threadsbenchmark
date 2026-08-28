package com.hospitaltelemetry.patient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryPatientRepositoryTest {
    private InMemoryPatientRepository repository;

    @BeforeEach
    void setUp(){
        repository = new InMemoryPatientRepository();
    }

    @Test
    void save_validPatient_shouldAssignIdAndReturnPatient(){
        Patient patient = createPatient("PAT-001");
        Patient savedPatient = repository.save(patient);
        assertSame(patient, savedPatient);
        assertEquals(1L, savedPatient.getId());
    }

    @Test
    void save_multiplePatients_shouldGenerateSequentialIds(){
        Patient firstPatient = createPatient("PAT-001");
        Patient secondPatient = createPatient("PAT-002");
        repository.save(firstPatient);
        repository.save(secondPatient);
        assertEquals(1L, firstPatient.getId());
        assertEquals(2L, secondPatient.getId());
    }

    @Test
    void save_samePatientTwice_shouldThrowIllegalStateException(){
        Patient patient = createPatient("PAT-001");
        repository.save(patient);
        assertThrows(
            IllegalStateException.class, () -> repository.save(patient)
        );
    }

    @Test
    void save_nullPatient_shouldThrowIllegalArgumentException(){
        assertThrows(
            IllegalArgumentException.class, () -> repository.save(null)
        );
    }

    @Test
    void findById_existingPatient_shouldReturnPatient(){
        Patient patient = repository.save(createPatient("PAT-001"));
        Optional<Patient> foundPatient = repository.findById(patient.getId());
        assertTrue(foundPatient.isPresent());
        assertSame(patient, foundPatient.get());
    }

    @Test
    void findById_missingPatient_shouldReturnEmptyOptional(){
        Optional<Patient> foundPatient = repository.findById(99L);
        assertTrue(foundPatient.isEmpty());
    }

    @Test
    void findById_nullId_shouldReturnEmptyOptional(){
        Optional<Patient> foundPatient = repository.findById(null);
        assertTrue(foundPatient.isEmpty());
    }

    @Test
    void findById_invalidId_shouldReturnEmptyOptional(){
        Optional<Patient> foundPatient = repository.findById(0L);
        assertTrue(foundPatient.isEmpty());
    }

    @Test
    void findByPatientCode_existingPatientCode_shouldReturnPatient(){
        Patient patient = repository.save(createPatient("PAT-001"));
        Optional<Patient> foundPatient = repository.findByPatientCode("PAT-001");
        assertTrue(foundPatient.isPresent());
        assertSame(patient, foundPatient.get());
    }

    @Test
    void findByPatientCode_missingPatientCode_shouldReturnEmptyOptional(){
        repository.save(createPatient("PAT-001"));
        Optional<Patient> foundPatient = repository.findByPatientCode("PAT-999");
        assertTrue(foundPatient.isEmpty());
    }

    @Test
    void findByPatientCode_nullPatientCode_shouldReturnEmptyOptional(){
        Optional<Patient> foundPatient = repository.findByPatientCode(null);
        assertTrue(foundPatient.isEmpty());
    }

    @Test
    void findByPatientCode_blankPatientCode_shouldReturnEmptyOptional(){
        Optional<Patient> foundPatient = repository.findByPatientCode(" ");
        assertTrue(foundPatient.isEmpty());
    }

    @Test
    void existsByPatientCode_existingPatientCode_shouldReturnTrue(){
        repository.save(createPatient("PAT-001"));
        boolean exists = repository.existsByPatientCode("PAT-001");
        assertTrue(exists);
    }

    @Test
    void existsByPatientCode_missingPatientCode_shouldReturnFalse(){
        boolean exists = repository.existsByPatientCode("PAT-999");
        assertFalse(exists);
    }

    @Test
    void existsByPatientCode_nullPatientCode_shouldReturnFalse(){
        boolean exists = repository.existsByPatientCode(null);
        assertFalse(exists);
    }

    @Test
    void existsByPatientCode_blankPatientCode_shouldReturnFalse(){
        boolean exists = repository.existsByPatientCode(" ");
        assertFalse(exists);
    }

    @Test
    void findAll_savedPatients_shouldReturnAllPatients(){
        Patient firstPatient = repository.save(createPatient("PAT-001"));
        Patient secondPatient = repository.save(createPatient("PAT-002"));
        List<Patient> patients = repository.findAll();
        assertEquals(2, patients.size());
        assertTrue(patients.contains(firstPatient));
        assertTrue(patients.contains(secondPatient));
    }

    @Test
    void findAll_noPatients_shouldReturnEmptyList(){
        List<Patient> patients = repository.findAll();
        assertTrue(patients.isEmpty());
    }

    private static Patient createPatient(String patientCode){
        return new Patient(
            patientCode,
            "ICU-A",
            "BED-01",
            Instant.parse("2026-08-28T10:00:00Z")
        );
    }
}