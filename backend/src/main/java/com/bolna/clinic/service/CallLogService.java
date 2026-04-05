package com.bolna.clinic.service;

import com.bolna.clinic.dto.AppointmentBookRequest;
import com.bolna.clinic.dto.BolnaWebhookPayload;
import com.bolna.clinic.entity.Appointment;
import com.bolna.clinic.entity.CallLog;
import com.bolna.clinic.entity.Patient;
import com.bolna.clinic.repository.AppointmentRepository;
import com.bolna.clinic.repository.CallLogRepository;
import com.bolna.clinic.repository.DoctorRepository;
import com.bolna.clinic.repository.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CallLogService {

    private static final Logger log = LoggerFactory.getLogger(CallLogService.class);

    private final CallLogRepository callLogRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentService appointmentService;

    public CallLogService(CallLogRepository callLogRepository,
                          AppointmentRepository appointmentRepository,
                          PatientRepository patientRepository,
                          DoctorRepository doctorRepository,
                          AppointmentService appointmentService) {
        this.callLogRepository = callLogRepository;
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.appointmentService = appointmentService;
    }

    public List<CallLog> getAllCallLogs() {
        return callLogRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Processes an incoming Bolna webhook:
     * 1. Creates a CallLog record
     * 2. If call completed with slot info, books an Appointment
     */
    @Transactional
    public CallLog processWebhook(BolnaWebhookPayload payload) {
        log.info("Processing Bolna webhook for call_id={}, status={}", payload.getCallId(), payload.getStatus());

        // Find existing appointment by callId if already initiated
        Optional<Appointment> existingAppointment = appointmentRepository.findByBolnaCallId(payload.getCallId());

        Appointment appointment = existingAppointment.orElse(null);

        // If call completed and we have slot preference data, attempt to book
        if ("completed".equalsIgnoreCase(payload.getStatus())
                && payload.getSlotPreference() != null
                && appointment == null) {
            appointment = tryBookAppointment(payload);
        }

        CallLog callLog = new CallLog();
        callLog.setBolnaCallId(payload.getCallId());
        callLog.setStatus(payload.getStatus());
        callLog.setTranscript(payload.getTranscript());
        callLog.setCreatedAt(LocalDateTime.now());
        callLog.setAppointment(appointment);

        CallLog saved = callLogRepository.save(callLog);
        log.info("CallLog saved: id={}", saved.getId());
        return saved;
    }

    private Appointment tryBookAppointment(BolnaWebhookPayload payload) {
        try {
            // Find doctor by name preference
            String doctorPref = payload.getDoctorPreference();
            Long doctorId = doctorRepository.findAll().stream()
                    .filter(d -> doctorPref != null && d.getName().toLowerCase().contains(doctorPref.toLowerCase()))
                    .findFirst()
                    .map(d -> d.getId())
                    .orElse(null);

            if (doctorId == null) {
                log.warn("No matching doctor found for preference '{}'", doctorPref);
                return null;
            }

            // Find patient by name (or phone stored in variables if provided)
            String patientName = payload.getPatientName();
            Optional<Patient> patient = patientRepository.findAll().stream()
                    .filter(p -> p.getName().equalsIgnoreCase(patientName))
                    .findFirst();

            if (patient.isEmpty()) {
                log.warn("No patient found with name '{}'", patientName);
                return null;
            }

            LocalDateTime slotTime = parseSlotTime(payload.getSlotPreference());
            if (slotTime == null) {
                log.warn("Could not parse slot preference '{}'", payload.getSlotPreference());
                return null;
            }

            AppointmentBookRequest req = new AppointmentBookRequest();
            req.setPatientId(patient.get().getId());
            req.setDoctorId(doctorId);
            req.setSlotTime(slotTime);
            req.setBolnaCallId(payload.getCallId());

            return appointmentService.bookAppointment(req);
        } catch (Exception e) {
            log.error("Failed to auto-book appointment from webhook: {}", e.getMessage(), e);
            return null;
        }
    }

    private LocalDateTime parseSlotTime(String slotPreference) {
        if (slotPreference == null) return null;
        try {
            return LocalDateTime.parse(slotPreference);
        } catch (Exception e) {
            log.warn("Could not parse slot time '{}': {}", slotPreference, e.getMessage());
            return null;
        }
    }
}
