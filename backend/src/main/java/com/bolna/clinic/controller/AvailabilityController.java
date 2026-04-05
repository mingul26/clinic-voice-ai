package com.bolna.clinic.controller;

import com.bolna.clinic.entity.Doctor;
import com.bolna.clinic.entity.Patient;
import com.bolna.clinic.repository.DoctorRepository;
import com.bolna.clinic.repository.PatientRepository;
import com.bolna.clinic.service.AppointmentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AvailabilityController {

    private final AppointmentService appointmentService;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public AvailabilityController(AppointmentService appointmentService,
                                   DoctorRepository doctorRepository,
                                   PatientRepository patientRepository) {
        this.appointmentService = appointmentService;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    @GetMapping("/availability")
    public ResponseEntity<Map<String, Object>> getAvailability(
            @RequestParam Long doctorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<String> slots = appointmentService.getAvailableSlots(doctorId, date);
        return ResponseEntity.ok(Map.of("doctorId", doctorId, "slots", slots));
    }

    @GetMapping("/doctors")
    public ResponseEntity<List<Doctor>> getAllDoctors() {
        return ResponseEntity.ok(doctorRepository.findAll());
    }

    @GetMapping("/patients")
    public ResponseEntity<List<Patient>> getAllPatients() {
        return ResponseEntity.ok(patientRepository.findAll());
    }
}
