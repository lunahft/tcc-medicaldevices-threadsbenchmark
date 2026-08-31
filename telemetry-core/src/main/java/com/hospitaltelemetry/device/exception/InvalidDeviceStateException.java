package com.hospitaltelemetry.device.exception;

public class InvalidDeviceStateException extends RuntimeException{
    public InvalidDeviceStateException(String message){
        super(message);
    }
}
