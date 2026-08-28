package com.hospitaltelemetry.device;

import java.util.List;
import java.util.Optional;

public interface DeviceRepository {

    MonitoringDevice save(MonitoringDevice device);
    Optional<MonitoringDevice> findById(Long id);
    Optional<MonitoringDevice> findByDeviceCode(String deviceCode);
    boolean existsByDeviceCode(String deviceCode);

    List<MonitoringDevice> findAll();
}