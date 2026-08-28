package com.hospitaltelemetry.device.exception;

public class DeviceNotAssignedException extends RuntimeException{
    public DeviceNotAssignedException(String message){
        super(message);
    }
}