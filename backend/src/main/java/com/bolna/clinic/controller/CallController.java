package com.bolna.clinic.controller;

import com.bolna.clinic.dto.OutboundCallRequest;
import com.bolna.clinic.entity.Doctor;
import com.bolna.clinic.entity.Patient;
import com.bolna.clinic.exception.ResourceNotFoundException;
import com.bolna.clinic.repository.DoctorRepository;
import com.bolna.clinic.repository.PatientRepository;
import com.bolna.clinic.service.BolnaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/calls")
public class CallController {

    private final BolnaService bolnaService;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public CallController(BolnaService bolnaService,
                          DoctorRepository doctorRepository,
                          PatientRepository patientRepository) {
        this.bolnaService = bolnaService;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    @PostMapping("/outbound")
    public ResponseEntity<Map<String, String>> triggerOutboundCall(
            @Valid @RequestBody OutboundCallRequest request) {

        // Resolve patient name/phone from DB if not provided
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + request.getPatientId()));

        if (request.getPatientName() == null || request.getPatientName().isBlank()) {
            request.setPatientName(patient.getName());
        }
        if (request.getPatientPhone() == null || request.getPatientPhone().isBlank()) {
            request.setPatientPhone(patient.getPhone());
        }
        if (request.getLanguage() == null || request.getLanguage().isBlank()) {
            request.setLanguage(patient.getLanguage());
        }

        // Resolve doctor
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found: " + request.getDoctorId()));

        String callId = bolnaService.initiateCall(request, doctor);

        return ResponseEntity.ok(Map.of(
                "callId", callId,
                "message", "Outbound call initiated successfully"
        ));
    }
}
