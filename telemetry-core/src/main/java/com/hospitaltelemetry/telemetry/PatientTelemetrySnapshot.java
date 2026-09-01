package com.hospitaltelemetry.telemetry;

import com.hospitaltelemetry.telemetry.TelemetryEvent;

public class PatientTelemetrySnapshot {
    private final Long patientId;
    private final TelemetryEvent heartRate;
    private final TelemetryEvent oxygenSaturation;
    private final TelemetryEvent bodyTemperature;
    private final TelemetryEvent respiratoryRate;
    private final TelemetryEvent systolicBloodPressure;
    private final TelemetryEvent diastolicBloodPressure;

    public PatientTelemetrySnapshot(
        Long patientId,
        TelemetryEvent heartRate,
        TelemetryEvent oxygenSaturation,
        TelemetryEvent bodyTemperature,
        TelemetryEvent respiratoryRate,
        TelemetryEvent systolicBloodPressure,
        TelemetryEvent diastolicBloodPressure
    ) {

        if(patientId == null || patientId <= 0){
            throw new IllegalArgumentException("patientId must not be null and must be greater than zero");
        }
        this.patientId = patientId;
        this.heartRate = heartRate;
        this.oxygenSaturation = oxygenSaturation;
        this.bodyTemperature = bodyTemperature;
        this.respiratoryRate = respiratoryRate;
        this.systolicBloodPressure = systolicBloodPressure;
        this.diastolicBloodPressure = diastolicBloodPressure;
    }

    public Long getPatientId() {
        return patientId;
    }

    public TelemetryEvent getHeartRate() {
        return heartRate;
    }

    public TelemetryEvent getOxygenSaturation() {
        return oxygenSaturation;
    }

    public TelemetryEvent getBodyTemperature() {
        return bodyTemperature;
    }

    public TelemetryEvent getRespiratoryRate() {
        return respiratoryRate;
    }

    public TelemetryEvent getSystolicBloodPressure() {
        return systolicBloodPressure;
    }

    public TelemetryEvent getDiastolicBloodPressure() {
        return diastolicBloodPressure;
    }
}
