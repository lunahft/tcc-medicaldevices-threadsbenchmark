package com.hospitaltelemetry.api.telemetry;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.hospitaltelemetry.device.exception.DeviceNotAssignedException;
import com.hospitaltelemetry.device.exception.DeviceNotFoundException;
import com.hospitaltelemetry.patient.exception.PatientNotFoundException;
import com.hospitaltelemetry.telemetry.exception.InvalidTelemetryException;

@RestControllerAdvice(assignableTypes = TelemetryController.class)
public class TelemetryExceptionHandler {

    @ExceptionHandler({PatientNotFoundException.class, DeviceNotFoundException.class})
    public ProblemDetail handleNotFound(RuntimeException exception) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                exception.getMessage()
        );
    }

    @ExceptionHandler({InvalidTelemetryException.class, DeviceNotAssignedException.class})
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
