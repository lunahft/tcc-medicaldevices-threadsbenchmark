package com.hospitaltelemetry.patient;

import java.time.Instant;
import java.util.List;

import com.hospitaltelemetry.patient.exception.DuplicatePatientCodeException;
import com.hospitaltelemetry.patient.exception.PatientNotFoundException;

public class PatientService {
    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository){
        this.patientRepository = patientRepository;
    }

    public Patient registerPatient(
        String patientCode,
        String unitCode,
        String bedCode,
        Instant admittedAt
    ) {
        if (patientRepository.existsByPatientCode(patientCode)){
            throw new DuplicatePatientCodeException(patientCode);
        }

        Patient patient = new Patient(
            patientCode,
            unitCode,
            bedCode,
            admittedAt
        );

        return patientRepository.save(patient);
    }

    public Patient findById(Long id){
        return patientRepository.findById(id)
            .orElseThrow(() -> new PatientNotFoundException(id));
    }

    public Patient findByPatientCode(String patientCode){
        return patientRepository.findByPatientCode(patientCode)
            .orElseThrow(() -> new PatientNotFoundException(patientCode));
    }

    public List<Patient> findAll(){
        return patientRepository.findAll();
    }

    public Patient dischargePatient(
        Long patientId,
        Instant dischargedAt
    ) {
        Patient patient = findById(patientId);
        patient.discharge(dischargedAt);

        return patient;
    }

    public Patient deactivatePatient(Long patientId) {
        Patient patient = findById(patientId);
        patient.deactivate();

        return patient;
    }
    
}
