package com.bolna.clinic.service;

import com.bolna.clinic.dto.AppointmentBookRequest;
import com.bolna.clinic.entity.Appointment;
import com.bolna.clinic.entity.Doctor;
import com.bolna.clinic.entity.DoctorSlot;
import com.bolna.clinic.entity.Patient;
import com.bolna.clinic.exception.ResourceNotFoundException;
import com.bolna.clinic.repository.AppointmentRepository;
import com.bolna.clinic.repository.DoctorRepository;
import com.bolna.clinic.repository.DoctorSlotRepository;
import com.bolna.clinic.repository.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorSlotRepository doctorSlotRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                               PatientRepository patientRepository,
                               DoctorRepository doctorRepository,
                               DoctorSlotRepository doctorSlotRepository) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.doctorSlotRepository = doctorSlotRepository;
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

        // Mark the slot as taken
        doctorSlotRepository
                .findByDoctorIdAndSlotTimeAndAvailableTrue(doctor.getId(), request.getSlotTime())
                .ifPresentOrElse(
                        slot -> { slot.setAvailable(false); doctorSlotRepository.save(slot); },
                        () -> log.warn("Slot {} not found or already taken for doctor {}", request.getSlotTime(), doctor.getId())
                );

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setSlotTime(request.getSlotTime());
        appointment.setStatus(Appointment.AppointmentStatus.CONFIRMED);
        appointment.setBolnaCallId(request.getBolnaCallId());

        Appointment saved = appointmentRepository.save(appointment);
        log.info("Appointment booked: id={}, patient={}, doctor={}, slot={}",
                saved.getId(), patient.getName(), doctor.getName(), request.getSlotTime());
        return saved;
    }

    @Transactional
    public Appointment updateStatus(Long id, Appointment.AppointmentStatus status) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + id));

        // Restore the slot when appointment is cancelled
        if (status == Appointment.AppointmentStatus.CANCELLED) {
            doctorSlotRepository
                    .findByDoctorIdAndSlotTimeAndAvailableTrue(
                            appointment.getDoctor().getId(), appointment.getSlotTime())
                    .ifPresentOrElse(
                            s -> {},  // already available, nothing to do
                            () -> doctorSlotRepository
                                    .findByDoctorIdAndSlotTime(
                                            appointment.getDoctor().getId(), appointment.getSlotTime())
                                    .ifPresent(s -> { s.setAvailable(true); doctorSlotRepository.save(s); })
                    );
        }

        appointment.setStatus(status);
        return appointmentRepository.save(appointment);
    }

    public List<String> getAvailableSlots(Long doctorId, LocalDate date) {
        doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found: " + doctorId));

        List<DoctorSlot> slots;
        if (date != null) {
            LocalDateTime from = date.atStartOfDay();
            LocalDateTime to = date.atTime(23, 59, 59);
            slots = doctorSlotRepository.findByDoctorIdAndSlotTimeBetweenAndAvailableTrueOrderBySlotTimeAsc(doctorId, from, to);
        } else {
            slots = doctorSlotRepository.findByDoctorIdAndAvailableTrueOrderBySlotTimeAsc(doctorId);
        }

        return slots.stream()
                .map(s -> s.getSlotTime().toString())
                .toList();
    }
}
