package com.hospitaltelemetry.patient;

import java.time.Instant;

public class Patient {
    private Long id;
    private String patientCode;
    private String unitCode;
    private String bedCode;
    private PatientStatus status;
    private Instant admittedAt;
    private Instant dischargedAt;
    
    public Patient(
        String patientCode,
        String unitCode,
        String bedCode,
        Instant admittedAt
    ) {

        if (admittedAt == null){
            throw new IllegalArgumentException("admittedAt cannot be null");
        }

        validateText(patientCode, "patientCode");
        validateText(unitCode, "unitCode");
        validateText(bedCode, "bedCode");


        this.patientCode = patientCode;
        this.unitCode = unitCode;
        this.bedCode = bedCode;
        this.admittedAt = admittedAt;
        this.status = PatientStatus.ACTIVE;
    }
    
    public Long getId(){
        return id;
    }
    public String getPatientCode(){
        return patientCode;
    }
    public String getUnitCode(){
        return unitCode;
    }
    public String getBedCode(){
        return bedCode;
    }
    public PatientStatus getStatus(){
        return status;
    }
    public Instant getAdmittedAt(){
        return admittedAt;
    }
    public Instant getDischargedAt(){
        return dischargedAt;
    }
    
    public void discharge(Instant dischargedAt){
        if (status == PatientStatus.DISCHARGED){
            throw new IllegalArgumentException("Patient is already discharged");
        }
        if (dischargedAt == null){
            throw new IllegalArgumentException("dischargedAt cannot be null");
        }
        if (dischargedAt.isBefore(admittedAt)){
            throw new IllegalArgumentException("dischargedAt cannot happen before admittedAt");
        }

        this.status = PatientStatus.DISCHARGED;
        this.dischargedAt = dischargedAt;
    }

    public void deactivate() {
        if (status == PatientStatus.DISCHARGED){
            throw new IllegalArgumentException("a Discharged patient cannot be deactivated");
        }
        this.status = PatientStatus.INACTIVE;
    }

    void assignId(Long id){
        if (this.id != null){
            throw new IllegalStateException("Patient already has an id");
        }

        if (id == null || id <=0){
            throw new IllegalArgumentException("Id must not be null and greater than zero");
        }
        this.id = id;
    }

    private static void validateText(String value, String fieldName){
        if (value == null || value.isBlank()){
            throw new IllegalArgumentException(fieldName + " cannot be null or blank");
        }
    }


}
