package com.hospitaltelemetry.telemetry;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.hospitaltelemetry.patient.PatientRepository;
import com.hospitaltelemetry.patient.exception.PatientNotFoundException;

public class TelemetryQueryService {
    private final TelemetryRepository telemetryRepository;
    private final PatientRepository patientRepository;

    public TelemetryQueryService(
        TelemetryRepository telemetryRepository,
        PatientRepository patientRepository
    ) {
        if(telemetryRepository == null){
            throw new IllegalArgumentException("telemetryRepository can't be null");
        }

        if(patientRepository == null){
            throw new IllegalArgumentException("patientRepository can't be null");
        }

        this.telemetryRepository = telemetryRepository;
        this.patientRepository = patientRepository;
    }

    public Optional<TelemetryEvent> getLatestMeasurement(
        Long patientId,
        MeasurementType type
    ) {
        ensurePatientExists(patientId);

        return telemetryRepository.findLatestByPatientIdAndType(patientId, type);
    }

    public List<TelemetryEvent> getHistory(
        Long patientId,
        Instant start,
        Instant end
    ) {
        ensurePatientExists(patientId);

        if(start == null || end == null){
            throw new IllegalArgumentException("start and end can't be null");
        }

        if(end.isBefore(start)){
            throw new IllegalArgumentException("end cannot be before start");
        }

        return telemetryRepository.findByPatientIdAndPeriod(patientId, start, end);
    }

    public PatientTelemetrySnapshot getSnapshot(
        Long patientId
    ) {
        ensurePatientExists(patientId);
        TelemetryEvent heartRate = telemetryRepository.findLatestByPatientIdAndType(patientId, MeasurementType.HEART_RATE).orElse(null);
        TelemetryEvent oxygenSaturation = telemetryRepository.findLatestByPatientIdAndType(patientId, MeasurementType.OXYGEN_SATURATION).orElse(null);
        TelemetryEvent bodyTemperature = telemetryRepository.findLatestByPatientIdAndType(patientId, MeasurementType.BODY_TEMPERATURE).orElse(null);
        TelemetryEvent respiratoryRate = telemetryRepository.findLatestByPatientIdAndType(patientId, MeasurementType.RESPIRATORY_RATE).orElse(null);
        TelemetryEvent systolicBloodPressure = telemetryRepository.findLatestByPatientIdAndType(patientId, MeasurementType.SYSTOLIC_BLOOD_PRESSURE).orElse(null);
        TelemetryEvent diastolicBloodPressure = telemetryRepository.findLatestByPatientIdAndType(patientId, MeasurementType.DIASTOLIC_BLOOD_PRESSURE).orElse(null);

        return new PatientTelemetrySnapshot(
            patientId,
            heartRate,
            oxygenSaturation,
            bodyTemperature,
            respiratoryRate,
            systolicBloodPressure,
            diastolicBloodPressure
        );
    }


    private void ensurePatientExists(Long patientId){
        patientRepository.findById(patientId)
            .orElseThrow(() -> new PatientNotFoundException(patientId));
    }
}
