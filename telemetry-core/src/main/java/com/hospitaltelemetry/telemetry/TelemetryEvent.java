package com.hospitaltelemetry.telemetry;

import java.time.Instant;

public class TelemetryEvent {
    private Long id;
    private Long patientId;
    private Long deviceId;
    private MeasurementType type;
    private double value;
    private Instant measuredAt;
    private Instant receivedAt;

    public TelemetryEvent(
        Long patientId,
        Long deviceId,
        MeasurementType type,
        double value,
        Instant measuredAt,
        Instant receivedAt
    ) {
        validateId(patientId, "patientId");
        validateId(deviceId, "deviceId");
        validateType(type);
        validateValue(value);
        validateInstant(measuredAt, "measuredAt");
        validateInstant(receivedAt, "receivedAt");
        validateChronology(measuredAt, receivedAt);

        this.patientId = patientId;
        this.deviceId = deviceId;
        this.type = type;
        this.value = value;
        this.measuredAt = measuredAt;
        this.receivedAt = receivedAt;
    }
    void assignId(Long id){
        if(this.id != null){
            throw new IllegalStateException("this event already has an id");
        }
        if(id == null || id <= 0){
            throw new IllegalArgumentException("id must not be null and must be greater than zero");
        }
        this.id = id;
    }
    public Long getId() {
        return id;
    }
    public Long getPatientId() {
        return patientId;
    }
    public Long getDeviceId() {
        return deviceId;
    }
    public MeasurementType getType() {
        return type;
    }
    public double getValue() {
        return value;
    }
    public Instant getMeasuredAt() {
        return measuredAt;
    }
    public Instant getReceivedAt() {
        return receivedAt;
    }

    private static void validateId(Long id, String fieldName) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(fieldName + " must not be null and greater than zero");
        }
    }

    private static void validateType(MeasurementType type) {
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }
    }

    private static void validateValue(double value) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("value must be a finite number");
        }
    }

    private static void validateInstant(Instant value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }
    }

    private static void validateChronology(Instant measuredAt, Instant receivedAt) {
        if (receivedAt.isBefore(measuredAt)) {
            throw new IllegalArgumentException("receivedAt cannot be before measuredAt");
        }
    }
}
