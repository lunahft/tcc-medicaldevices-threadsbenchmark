package com.hospitaltelemetry.api.device;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.hospitaltelemetry.api.device.dto.DeviceResponse;
import com.hospitaltelemetry.api.device.dto.RegisterDeviceRequest;
import com.hospitaltelemetry.device.DeviceService;
import com.hospitaltelemetry.device.MonitoringDevice;

@RestController
@RequestMapping("/devices")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping
    public ResponseEntity<DeviceResponse> registerDevice(
            @Valid @RequestBody RegisterDeviceRequest request) {

        MonitoringDevice device = deviceService.registerDevice(
                request.deviceCode(),
                request.type(),
                request.registeredAt()
        );

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(device.getId())
                .toUri();

        return ResponseEntity.created(location)
                .body(DeviceResponse.from(device));
    }

    @GetMapping
    public List<DeviceResponse> findAll() {
        return deviceService.findAll()
                .stream()
                .map(DeviceResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public DeviceResponse findById(@PathVariable("id") Long id) {
        return DeviceResponse.from(deviceService.findById(id));
    }

    @GetMapping("/by-code")
    public DeviceResponse findByCode(@RequestParam("deviceCode") String deviceCode) {
        return DeviceResponse.from(deviceService.findByCode(deviceCode));
    }

    @PatchMapping("/{id}/assign")
    public DeviceResponse assignToPatient(
            @PathVariable("id") Long id,
            @RequestParam("patientId") Long patientId) {

        return DeviceResponse.from(
                deviceService.assignDeviceToPatient(id, patientId)
        );
    }

    @PatchMapping("/{id}/unassign")
    public DeviceResponse unassignFromPatient(
            @PathVariable("id") Long id,
            @RequestParam("patientId") Long patientId) {

        return DeviceResponse.from(
                deviceService.unassignDevice(id, patientId)
        );
    }

    @PatchMapping("/{id}/maintenance")
    public DeviceResponse sendToMaintenance(@PathVariable("id") Long id) {
        return DeviceResponse.from(deviceService.sendToMaintenance(id));
    }

    @PatchMapping("/{id}/activate")
    public DeviceResponse activateDevice(@PathVariable("id") Long id) {
        return DeviceResponse.from(deviceService.activateDevice(id));
    }

    @PatchMapping("/{id}/deactivate")
    public DeviceResponse deactivateDevice(@PathVariable("id") Long id) {
        return DeviceResponse.from(deviceService.deactivateDevice(id));
    }
}
