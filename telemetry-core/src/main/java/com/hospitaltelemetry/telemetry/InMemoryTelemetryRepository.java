package com.hospitaltelemetry.telemetry;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

// TEMPORARY CLASS. IMPLEMENTING DATABASE CONNECTION LATER
public class InMemoryTelemetryRepository implements TelemetryRepository {

    private final Map<Long, TelemetryEvent> events = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    @Override
    public TelemetryEvent save(TelemetryEvent event){
        
        if(event == null){
            throw new IllegalArgumentException("event cannot be null");
        }

        Long id = nextId.getAndIncrement();
        event.assignId(id);
        events.put(id, event);

        return event;
    }

    @Override
    public Optional<TelemetryEvent> findLatestByPatientIdAndType(
        Long patientId,
        MeasurementType type
    ) {
        if(patientId == null || patientId <= 0 || type == null){
            return Optional.empty();
        }

        return events.values().stream()
            .filter(event -> event.getPatientId().equals(patientId))
            .filter(event -> event.getType() == type)
            .max(Comparator.comparing(TelemetryEvent::getMeasuredAt));
    }

    @Override
    public List<TelemetryEvent> findByPatientIdAndPeriod(
        Long patientId,
        Instant start,
        Instant end
    ) {
        if (patientId == null || patientId <= 0){
            return List.of();
        }

        if(start == null || end == null){
            throw new IllegalArgumentException("start and end cannot be null");
        }

        if(end.isBefore(start)){
            throw new IllegalArgumentException("end must not be before start");
        }

        return events.values().stream()
            .filter(event -> event.getPatientId().equals(patientId))
            .filter(event -> !event.getMeasuredAt().isBefore(start) && !event.getMeasuredAt().isAfter(end))
            .sorted(Comparator.comparing(TelemetryEvent::getMeasuredAt))
            .toList();
    }

    @Override
    public long count(){
        return events.size();
    }
}
