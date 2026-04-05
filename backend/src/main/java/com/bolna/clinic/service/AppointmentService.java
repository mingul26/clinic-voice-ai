package com.bolna.clinic.service;

import com.bolna.clinic.dto.AppointmentBookRequest;
import com.bolna.clinic.entity.Appointment;
import com.bolna.clinic.entity.Doctor;
import com.bolna.clinic.entity.Patient;
import com.bolna.clinic.exception.ResourceNotFoundException;
import com.bolna.clinic.repository.AppointmentRepository;
import com.bolna.clinic.repository.DoctorRepository;
import com.bolna.clinic.repository.PatientRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AppointmentService(AppointmentRepository appointmentRepository,
                               PatientRepository patientRepository,
                               DoctorRepository doctorRepository) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAllByOrderBySlotTimeDesc();
    }

    @Transactional
    public Appointment bookAppointment(AppointmentBookRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + request.getPatientId()));

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found: " + request.getDoctorId()));

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setSlotTime(request.getSlotTime());
        appointment.setStatus(Appointment.AppointmentStatus.CONFIRMED);
        appointment.setBolnaCallId(request.getBolnaCallId());

        // Remove the booked slot from doctor's available slots
        removeSlotFromDoctor(doctor, request.getSlotTime());

        Appointment saved = appointmentRepository.save(appointment);
        log.info("Appointment booked: id={}, patient={}, doctor={}, slot={}",
                saved.getId(), patient.getName(), doctor.getName(), request.getSlotTime());
        return saved;
    }

    @Transactional
    public Appointment updateStatus(Long id, Appointment.AppointmentStatus status) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + id));
        appointment.setStatus(status);
        return appointmentRepository.save(appointment);
    }

    public List<String> getAvailableSlots(Long doctorId, LocalDate date) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found: " + doctorId));

        List<String> allSlots = parseSlotsJson(doctor.getAvailableSlots());

        if (date == null) {
            return allSlots;
        }

        return allSlots.stream()
                .filter(slot -> {
                    try {
                        LocalDateTime dt = LocalDateTime.parse(slot);
                        return dt.toLocalDate().equals(date);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }

    private void removeSlotFromDoctor(Doctor doctor, LocalDateTime slot) {
        try {
            List<String> slots = parseSlotsJson(doctor.getAvailableSlots());
            String slotStr = slot.toString();
            slots.remove(slotStr);
            doctor.setAvailableSlots(objectMapper.writeValueAsString(slots));
            doctorRepository.save(doctor);
        } catch (Exception e) {
            log.warn("Could not remove slot from doctor availability: {}", e.getMessage());
        }
    }

    private List<String> parseSlotsJson(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse doctor slots JSON: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
