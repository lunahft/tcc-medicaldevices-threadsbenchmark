package com.hospitaltelemetry.patient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
// THIS CLASS IS TEMPORARY, BUILT FOR TESTS. IMPLEMENTING DATABASE CONNECTION LATER.

public class InMemoryPatientRepository implements PatientRepository {
    private final Map<Long, Patient> patients = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    @Override
    public Patient save(Patient patient){
        if (patient == null){
            throw new IllegalArgumentException("patient can't be null");
        }
        Long id = nextId.getAndIncrement();
        patient.assignId(id);
        patients.put(id, patient);
        return patient;
    }

    @Override
    public Optional<Patient> findById(Long id){
        if (id == null || id <= 0){
            return Optional.empty();
        }
        return Optional.ofNullable(patients.get(id));
    }

    @Override
    public Optional<Patient> findByPatientCode(String patientCode){
        if (patientCode == null || patientCode.isBlank()){
            return Optional.empty();
        }
        return patients.values().stream()
            .filter(patient -> patient.getPatientCode().equals(patientCode))
            .findFirst();
    }

    @Override
    public boolean existsByPatientCode(String patientCode){
        if (patientCode == null || patientCode.isBlank()){
            return false;
        }
        return findByPatientCode(patientCode).isPresent();
    }

    @Override
    public List<Patient> findAll(){
        return new ArrayList<>(patients.values());
    }
}
