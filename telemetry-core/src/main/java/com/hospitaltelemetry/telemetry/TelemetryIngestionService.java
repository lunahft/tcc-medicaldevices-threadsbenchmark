package com.hospitaltelemetry.telemetry;

import java.time.Instant;

import com.hospitaltelemetry.device.DeviceRepository;
import com.hospitaltelemetry.device.MonitoringDevice;
import com.hospitaltelemetry.device.exception.DeviceNotAssignedException;
import com.hospitaltelemetry.device.exception.DeviceNotFoundException;
import com.hospitaltelemetry.patient.Patient;
import com.hospitaltelemetry.patient.PatientRepository;
import com.hospitaltelemetry.patient.PatientStatus;
import com.hospitaltelemetry.patient.exception.PatientNotFoundException;
import com.hospitaltelemetry.telemetry.exception.InvalidTelemetryException;

public class TelemetryIngestionService {
    private final DeviceRepository deviceRepository;
    private final PatientRepository patientRepository;
    private final TelemetryRepository telemetryRepository;

    public TelemetryIngestionService(
        DeviceRepository deviceRepository, 
        PatientRepository patientRepository, 
        TelemetryRepository telemetryRepository
    ) {
        if(deviceRepository == null){
            throw new IllegalArgumentException("deviceRepository can't be null");
        }
        if(patientRepository == null){
            throw new IllegalArgumentException("patientRepository can't be null");
        }
        if(telemetryRepository == null){
            throw new IllegalArgumentException("telemetryRepository can't be null");
        }

        this.deviceRepository = deviceRepository;
        this.patientRepository = patientRepository;
        this.telemetryRepository = telemetryRepository;
    }

    public TelemetryEvent ingest(
        Long patientId,
        Long deviceId,
        MeasurementType type,
        double value,
        Instant measuredAt
    ) {
        Patient patient = findPatient(patientId);
        validatePatient(patient);

        MonitoringDevice device = findDevice(deviceId);
        validateDevice(device);

        validateAssignment(device, patientId);

        Instant receivedAt = Instant.now();

        TelemetryEvent event = new TelemetryEvent(
            patientId, 
            deviceId, 
            type, 
            value, 
            measuredAt, 
            receivedAt
        );
        
        TelemetryEvent savedEvent = telemetryRepository.save(event);
        
        device.markSeenAt(receivedAt);

        return savedEvent;
    }

    private Patient findPatient(Long patientId){
        return patientRepository.findById(patientId)
            .orElseThrow(() -> new PatientNotFoundException(patientId));
    }

    private void validatePatient(Patient patient){
        if(patient.getStatus() != PatientStatus.ACTIVE){
            throw new InvalidTelemetryException("Telemetry Cannot be ingested for non-active patient");
        }
    }

    private MonitoringDevice findDevice(Long deviceId){
        return deviceRepository.findById(deviceId)
            .orElseThrow(() -> new DeviceNotFoundException(deviceId));
    }

    private void validateDevice(MonitoringDevice device){
        if (!device.isActive()){
            throw new InvalidTelemetryException("Telemetry cannot be ingested for non-active device");
        }
    }

    private void validateAssignment(MonitoringDevice device, Long patientId){
        if(!device.isAssigned()){
            throw new DeviceNotAssignedException(device.getId());
        }
        if(!device.getPatientId().equals(patientId)){
            throw new InvalidTelemetryException("Device is not assigned to the informed patient");
        }
    }
}
