package com.hospitaltelemetry.device;

import com.hospitaltelemetry.device.exception.DeviceNotAssignedException;
import com.hospitaltelemetry.device.exception.DeviceNotFoundException;
import com.hospitaltelemetry.device.exception.DuplicateDeviceCodeException;
import com.hospitaltelemetry.device.exception.InvalidDeviceStateException;
import com.hospitaltelemetry.patient.Patient;
import com.hospitaltelemetry.patient.PatientRepository;
import com.hospitaltelemetry.patient.PatientStatus;
import com.hospitaltelemetry.patient.exception.PatientNotFoundException;

import java.time.Instant;
import java.util.List;

public class DeviceService {
    private final DeviceRepository deviceRepository;
    private final PatientRepository patientRepository;

    public DeviceService(
        DeviceRepository deviceRepository,
        PatientRepository patientRepository
    ) {
        if(deviceRepository == null){
            throw new IllegalArgumentException("deviceRepository can't be null");
        }
        if(patientRepository == null){
            throw new IllegalArgumentException("patientRepository can't be null");
        }

        this.deviceRepository = deviceRepository;
        this.patientRepository = patientRepository;
    }

    public MonitoringDevice registerDevice(
        String deviceCode,
        DeviceType deviceType,
        Instant registeredAt
    ) {
        if (deviceRepository.existsByDeviceCode(deviceCode)){
            throw new DuplicateDeviceCodeException(deviceCode);
        }
        MonitoringDevice monitoringDevice = new MonitoringDevice(
            deviceCode,
            deviceType,
            registeredAt
        );
        
        return deviceRepository.save(monitoringDevice);
    }

    public MonitoringDevice findById(Long id){
        return deviceRepository.findById(id).orElseThrow(() -> new DeviceNotFoundException(id));
    }

    public MonitoringDevice findByCode(String deviceCode){
        return deviceRepository.findByDeviceCode(deviceCode).orElseThrow(() -> new DeviceNotFoundException(deviceCode));
    }

    public List<MonitoringDevice> findAll(){
        return deviceRepository.findAll();
    }

    public MonitoringDevice assignDeviceToPatient(Long deviceId, Long patientId){
        MonitoringDevice device = findById(deviceId);

        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> new PatientNotFoundException(patientId)
        );

        if (patient.getStatus() != PatientStatus.ACTIVE){
            throw new InvalidDeviceStateException("Device can only be assigned to an active patient");
        }

        if(!device.isActive()){
            throw new InvalidDeviceStateException("Device must be active to be assigned");
        }

        if(device.isAssigned()){
            throw new InvalidDeviceStateException("Device is already assigned to a patient");
        }

        device.assignToPatient(patientId);

        return device;
    }

    public MonitoringDevice unassignDevice(Long deviceId, Long patientId){
        MonitoringDevice device = findById(deviceId);

        if (!device.isAssigned()){
            throw new DeviceNotAssignedException("Device is not assigned with id: " + device.getId());
        }

        device.unassignPatient();

        return device;
    }

    public MonitoringDevice sendToMaintenance(Long deviceId){
        MonitoringDevice device = findById(deviceId);

        device.sendToMaintenance();
        return device;
    }

    public MonitoringDevice activateDevice(Long deviceId){
        MonitoringDevice device = findById(deviceId);

        device.activate();

        return device;
    }

    public MonitoringDevice deactivateDevice(Long deviceId){
        MonitoringDevice device = findById(deviceId);

        device.deactivate();

        return device;
    }


}
