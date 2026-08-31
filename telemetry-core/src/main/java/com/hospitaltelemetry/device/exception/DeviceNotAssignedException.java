package com.hospitaltelemetry.device.exception;

public class DeviceNotAssignedException extends RuntimeException{
    public DeviceNotAssignedException(String message){
        super(message);
    }
    public DeviceNotAssignedException(Long deviceId){
        super("Device is not assigned with id: " + deviceId);
    }
}