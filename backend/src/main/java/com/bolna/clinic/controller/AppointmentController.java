package com.bolna.clinic.controller;

import com.bolna.clinic.dto.AppointmentBookRequest;
import com.bolna.clinic.entity.Appointment;
import com.bolna.clinic.entity.CallLog;
import com.bolna.clinic.service.AppointmentService;
import com.bolna.clinic.service.CallLogService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final CallLogService callLogService;

    public AppointmentController(AppointmentService appointmentService, CallLogService callLogService) {
        this.appointmentService = appointmentService;
        this.callLogService = callLogService;
    }

    @GetMapping("/appointments")
    public ResponseEntity<List<Appointment>> getAllAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    @PostMapping("/appointments/book")
    public ResponseEntity<Appointment> bookAppointment(@Valid @RequestBody AppointmentBookRequest request) {
        return ResponseEntity.ok(appointmentService.bookAppointment(request));
    }

    @PatchMapping("/appointments/{id}/status")
    public ResponseEntity<Appointment> updateStatus(
            @PathVariable Long id,
            @RequestParam Appointment.AppointmentStatus status) {
        return ResponseEntity.ok(appointmentService.updateStatus(id, status));
    }

    @GetMapping("/call-logs")
    public ResponseEntity<List<CallLog>> getCallLogs() {
        return ResponseEntity.ok(callLogService.getAllCallLogs());
    }
}
