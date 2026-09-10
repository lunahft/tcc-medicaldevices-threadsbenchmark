package com.hospitaltelemetry.api.patient;

import java.net.URI;
import java.time.Instant;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.hospitaltelemetry.patient.Patient;
import com.hospitaltelemetry.patient.PatientService;
import com.hospitaltelemetry.patient.PatientStatus;
import com.hospitaltelemetry.patient.exception.DuplicatePatientCodeException;
import com.hospitaltelemetry.patient.exception.PatientNotFoundException;

@RestController
@RequestMapping("/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping
    public ResponseEntity<PatientResponse> registerPatient(@Valid @RequestBody RegisterPatientRequest request) {

        Patient patient = patientService.registerPatient(
                request.patientCode(),
                request.unitCode(),
                request.bedCode(),
                request.admittedAt()
        );

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(patient.getId())
                .toUri();

        return ResponseEntity.created(location)
                .body(PatientResponse.from(patient));
    }

    @GetMapping
    public List<PatientResponse> findAll() {
        return patientService.findAll()
                .stream()
                .map(PatientResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public PatientResponse findById(@PathVariable("id") Long id) {
        return PatientResponse.from(patientService.findById(id));
    }

    @GetMapping("/by-code")
    public PatientResponse findByPatientCode(@RequestParam("patientCode") String patientCode) {

        return PatientResponse.from(
                patientService.findByPatientCode(patientCode)
        );
    }

    @PatchMapping("/{id}/discharge")
    public PatientResponse dischargePatient(
            @PathVariable("id") Long id,
            @Valid @RequestBody DischargePatientRequest request) {

        Patient patient = patientService.dischargePatient(
                id,
                request.dischargedAt()
        );

        return PatientResponse.from(patient);
    }

    @PatchMapping("/{id}/deactivate")
    public PatientResponse deactivatePatient(
            @PathVariable("id") Long id) {

        return PatientResponse.from(
                patientService.deactivatePatient(id)
        );
    }

    @ExceptionHandler(PatientNotFoundException.class)
    public ProblemDetail handlePatientNotFound(
            PatientNotFoundException exception) {

        return ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                exception.getMessage()
        );
    }

    @ExceptionHandler(DuplicatePatientCodeException.class)
    public ProblemDetail handleDuplicatePatientCode(
            DuplicatePatientCodeException exception) {

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

    public record RegisterPatientRequest(
            @NotBlank String patientCode,
            @NotBlank String unitCode,
            @NotBlank String bedCode,
            @NotNull Instant admittedAt
    ) {
    }

    public record DischargePatientRequest(
            @NotNull Instant dischargedAt
    ) {
    }

    public record PatientResponse(
            Long id,
            String patientCode,
            String unitCode,
            String bedCode,
            PatientStatus status,
            Instant admittedAt,
            Instant dischargedAt
    ) {
        public static PatientResponse from(Patient patient) {
            return new PatientResponse(
                    patient.getId(),
                    patient.getPatientCode(),
                    patient.getUnitCode(),
                    patient.getBedCode(),
                    patient.getStatus(),
                    patient.getAdmittedAt(),
                    patient.getDischargedAt()
            );
        }
    }
}