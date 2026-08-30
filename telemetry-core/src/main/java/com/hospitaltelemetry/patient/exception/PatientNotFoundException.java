package com.hospitaltelemetry.patient.exception;

public class PatientNotFoundException extends RuntimeException{
    public PatientNotFoundException(Long id){
        super("Patient not found with id: " + id);
    }
    public PatientNotFoundException(String patientCode){
        super("Patient not found with code: " + patientCode);
    }
}
