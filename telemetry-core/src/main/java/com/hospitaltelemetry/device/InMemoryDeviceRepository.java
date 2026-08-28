package com.hospitaltelemetry.device;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

// THIS CLASS IS TEMPORARY, BUILT FOR TESTS. IMPLEMENTING DATABASE CONNECTION LATER.
public class InMemoryDeviceRepository implements DeviceRepository {
    private final Map<Long, MonitoringDevice> devices = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    @Override
    public MonitoringDevice save(MonitoringDevice device){
        if (device == null){
            throw new IllegalArgumentException("device can't be null");
        }
        Long id = nextId.getAndIncrement();
        device.assignId(id);
        devices.put(id, device);
        return device;
    }

    @Override
    public Optional<MonitoringDevice> findById(Long id){
        if (id == null || id <= 0){
            return Optional.empty();
        }
        return Optional.ofNullable(devices.get(id));
    }

    @Override
    public Optional<MonitoringDevice> findByDeviceCode(String deviceCode){
        if (deviceCode == null || deviceCode.isBlank()){
            return Optional.empty();
        }
        return devices.values().stream()
            .filter(device -> device.getDeviceCode().equals(deviceCode))
            .findFirst();
    }

    @Override
    public boolean existsByDeviceCode(String deviceCode){
        if (deviceCode == null || deviceCode.isBlank()){
            return false;
        }
        return findByDeviceCode(deviceCode).isPresent();
    }

    @Override
    public List<MonitoringDevice> findAll(){
        return new ArrayList<>(devices.values());
    }
}