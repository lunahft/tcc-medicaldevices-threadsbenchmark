package com.hospitaltelemetry.device.exception;

public class DeviceNotFoundException extends RuntimeException{
    public DeviceNotFoundException(Long id){
        super("Device was not found with id: " + id);
    }
    public DeviceNotFoundException(String deviceCode){
        super("Device was not found with code: " + deviceCode);
    }
}
