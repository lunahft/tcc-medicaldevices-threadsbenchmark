package com.hospitaltelemetry.api.device.dto;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.hospitaltelemetry.device.DeviceType;

public record RegisterDeviceRequest(
        @NotBlank String deviceCode,
        @NotNull DeviceType type,
        @NotNull Instant registeredAt
) {
}
