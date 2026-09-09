package com.hospitaltelemetry.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

import com.hospitaltelemetry.patient.PatientRepository;
import com.hospitaltelemetry.patient.PatientService;
import com.hospitaltelemetry.patient.InMemoryPatientRepository;
import com.hospitaltelemetry.device.DeviceRepository;
import com.hospitaltelemetry.device.DeviceService;
import com.hospitaltelemetry.device.InMemoryDeviceRepository;
import com.hospitaltelemetry.telemetry.TelemetryRepository;
import com.hospitaltelemetry.telemetry.InMemoryTelemetryRepository;
import com.hospitaltelemetry.telemetry.TelemetryIngestionService;
import com.hospitaltelemetry.telemetry.TelemetryQueryService;

@Configuration
public class CoreConfig {

    @Bean
    public PatientRepository patientRepository(){
        return new InMemoryPatientRepository();
    }

    @Bean 
    public DeviceRepository deviceRepository(){
        return new InMemoryDeviceRepository();
    }

    @Bean 
    public TelemetryRepository telemetryRepository(){
        return new InMemoryTelemetryRepository();
    }

    @Bean
    public PatientService patientService(PatientRepository patientRepository){
        return new PatientService(patientRepository);
    }

    @Bean
    public DeviceService deviceService(
        DeviceRepository deviceRepository,
        PatientRepository patientRepository
    ) {
        return new DeviceService(
            deviceRepository, 
            patientRepository
        );
    }

    @Bean
    public TelemetryIngestionService telemetryIngestionService(
        DeviceRepository deviceRepository,
        PatientRepository patientRepository,
        TelemetryRepository telemetryRepository
    ) {
        return new TelemetryIngestionService(
            deviceRepository, 
            patientRepository, 
            telemetryRepository
        );
    }

    @Bean
    public TelemetryQueryService telemetryQueryService(
        TelemetryRepository telemetryRepository,
        PatientRepository patientRepository
    ) {
        return new TelemetryQueryService(
            telemetryRepository, 
            patientRepository
        );
    }
}