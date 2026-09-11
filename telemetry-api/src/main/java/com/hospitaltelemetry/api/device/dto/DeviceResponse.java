package com.hospitaltelemetry.api.device.dto;

import java.time.Instant;

import com.hospitaltelemetry.device.DeviceStatus;
import com.hospitaltelemetry.device.DeviceType;
import com.hospitaltelemetry.device.MonitoringDevice;

public record DeviceResponse(
        Long id,
        String deviceCode,
        DeviceType type,
        DeviceStatus status,
        Long patientId,
        Instant registeredAt,
        Instant lastSeenAt
) {

    public static DeviceResponse from(MonitoringDevice device) {
        return new DeviceResponse(
                device.getId(),
                device.getDeviceCode(),
                device.getType(),
                device.getStatus(),
                device.getPatientId(),
                device.getRegisteredAt(),
                device.getLastSeenAt()
        );
    }
}
