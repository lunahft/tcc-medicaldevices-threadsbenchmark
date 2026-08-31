package com.hospitaltelemetry.device.exception;

public class DuplicateDeviceCodeException extends RuntimeException {
    public DuplicateDeviceCodeException(String deviceCode){
        super("Device already exists containing code: " + deviceCode);
    }
}
