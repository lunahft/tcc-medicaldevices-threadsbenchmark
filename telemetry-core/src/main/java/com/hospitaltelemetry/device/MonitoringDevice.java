package com.hospitaltelemetry.device;

import java.time.Instant;

import com.hospitaltelemetry.device.exception.DeviceNotAssignedException;
import com.hospitaltelemetry.device.exception.InvalidDeviceStateException;

public class MonitoringDevice {
    private Long id;
    private String deviceCode;
    private DeviceType type;
    private DeviceStatus status;
    private Long patientId;
    private Instant registeredAt;
    private Instant lastSeenAt;

    public MonitoringDevice(
        String deviceCode,
        DeviceType type,
        Instant registeredAt
    ) {
        validateText(deviceCode, "deviceCode");
        validateType(type);
        validateInstant(registeredAt, "registeredAt");

        this.deviceCode = deviceCode;
        this.type = type;
        this.registeredAt = registeredAt;
        this.status = DeviceStatus.ACTIVE;
    }

    public void assignToPatient(Long patientId){
        if (patientId == null || patientId <= 0){
            throw new IllegalArgumentException("patientId must not be null and greater than zero");
        }
        if(this.patientId != null){
            throw new IllegalStateException("Device is already assigned to a patient");
        }
        this.patientId = patientId;
    }

    public void unassignPatient(){
        if(this.patientId == null){
            throw new DeviceNotAssignedException("Device is not assigned to a patient");
        }
        this.patientId = null;
    }

    public void markSeenAt(Instant lastSeenAt){
        validateInstant(lastSeenAt, "lastSeenAt");
        if (lastSeenAt.isBefore(registeredAt)){
            throw new IllegalArgumentException("lastSeenAt cannot happen before registeredAt");
        }
        this.lastSeenAt = lastSeenAt;
    }

    public void deactivate(){
        if(status == DeviceStatus.INACTIVE){
            throw new IllegalStateException("Device is already inactive");
        }
        this.status = DeviceStatus.INACTIVE;
    }

    public void sendToMaintenance(){
        this.status = DeviceStatus.MAINTENANCE;
    }

    public void activate(){
        if(status == DeviceStatus.ACTIVE){
            throw new IllegalStateException("Device is already active");
        }
        this.status = DeviceStatus.ACTIVE;
    }

    public Long getId(){
        return id;
    }
    public String getDeviceCode(){
        return deviceCode;
    }
    public DeviceType getType(){
        return type;
    }
    public DeviceStatus getStatus(){
        return status;
    }
    public Long getPatientId(){
        return patientId;
    }
    public Instant getRegisteredAt(){
        return registeredAt;
    }
    public Instant getLastSeenAt(){
        return lastSeenAt;
    }

    public boolean isActive(){
        return this.status == DeviceStatus.ACTIVE;
    }
    public boolean isAssigned(){
        return this.patientId != null;
    }

    void assignId(Long id){
        if (this.id != null){
            throw new IllegalStateException("MonitoringDevice already has an id");
        }

        if (id == null || id <= 0){
            throw new IllegalArgumentException("Id must not be null and greater than zero");
        }

        this.id = id;
    }

    private static void validateText(String value, String fieldName){
        if (value == null || value.isBlank()){
            throw new IllegalArgumentException(fieldName + " cannot be null or blank");
        }
    }

    private static void validateType(DeviceType type){
        if (type == null){
            throw new IllegalArgumentException("type cannot be null");
        }
    }

    private static void validateInstant(Instant value, String fieldName){
        if (value == null){
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }
    }
}