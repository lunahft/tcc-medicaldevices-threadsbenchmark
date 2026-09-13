package com.hospitaltelemetry.api.telemetry;

import java.time.Instant;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hospitaltelemetry.api.telemetry.dto.TelemetryRequest;
import com.hospitaltelemetry.api.telemetry.dto.TelemetryResponse;
import com.hospitaltelemetry.telemetry.MeasurementType;
import com.hospitaltelemetry.telemetry.TelemetryEvent;
import com.hospitaltelemetry.telemetry.TelemetryIngestionService;
import com.hospitaltelemetry.telemetry.TelemetryQueryService;

@RestController
@RequestMapping("/telemetry")
public class TelemetryController {

    private final TelemetryIngestionService telemetryIngestionService;
    private final TelemetryQueryService telemetryQueryService;

    public TelemetryController(
            TelemetryIngestionService telemetryIngestionService,
            TelemetryQueryService telemetryQueryService
        ) {
        this.telemetryIngestionService = telemetryIngestionService;
        this.telemetryQueryService = telemetryQueryService;
    }

    @PostMapping
    public ResponseEntity<TelemetryResponse> ingest(@Valid @RequestBody TelemetryRequest request) {
        TelemetryEvent event = telemetryIngestionService.ingest(
                request.patientId(),
                request.deviceId(),
                request.type(),
                request.value(),
                request.measuredAt()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TelemetryResponse.from(event));
    }

    @GetMapping("/latest")
    public ResponseEntity<TelemetryResponse> getLatestMeasurement(
            @RequestParam("patientId") Long patientId,
            @RequestParam("type") MeasurementType type
        ) {
        return telemetryQueryService.getLatestMeasurement(patientId, type)
                .map(TelemetryResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/history")
    public List<TelemetryResponse> getHistory(
            @RequestParam("patientId") Long patientId,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end
        ) {
        return telemetryQueryService.getHistory(patientId, start, end)
                .stream()
                .map(TelemetryResponse::from)
                .toList();
    }
}
