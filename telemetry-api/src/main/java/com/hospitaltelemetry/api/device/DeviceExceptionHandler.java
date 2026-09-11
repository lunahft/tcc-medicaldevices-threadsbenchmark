package com.hospitaltelemetry.api.device;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.hospitaltelemetry.device.exception.DeviceNotAssignedException;
import com.hospitaltelemetry.device.exception.DeviceNotFoundException;
import com.hospitaltelemetry.device.exception.DuplicateDeviceCodeException;
import com.hospitaltelemetry.device.exception.InvalidDeviceStateException;
import com.hospitaltelemetry.patient.exception.PatientNotFoundException;

@RestControllerAdvice(assignableTypes = DeviceController.class)
public class DeviceExceptionHandler {

    @ExceptionHandler({DeviceNotFoundException.class, PatientNotFoundException.class})
    public ProblemDetail handleNotFound(RuntimeException exception) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                exception.getMessage()
        );
    }

    @ExceptionHandler(DuplicateDeviceCodeException.class)
    public ProblemDetail handleDuplicateDeviceCode(DuplicateDeviceCodeException exception) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );
    }

    @ExceptionHandler({
            DeviceNotAssignedException.class,
            InvalidDeviceStateException.class,
            IllegalStateException.class
    })
    public ProblemDetail handleInvalidState(RuntimeException exception) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleInvalidArgument(IllegalArgumentException exception) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );
    }
}
