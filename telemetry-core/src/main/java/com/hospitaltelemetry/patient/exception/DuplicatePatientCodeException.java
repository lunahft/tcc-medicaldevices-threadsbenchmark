package com.hospitaltelemetry.patient.exception;

public class DuplicatePatientCodeException extends RuntimeException{
    public DuplicatePatientCodeException(String patientCode){
        super("Patient code already exists: " + patientCode);
    }
}
