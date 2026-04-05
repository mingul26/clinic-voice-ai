package com.bolna.clinic.service;

import com.bolna.clinic.dto.AppointmentBookRequest;
import com.bolna.clinic.dto.BolnaWebhookPayload;
import com.bolna.clinic.entity.Appointment;
import com.bolna.clinic.entity.CallLog;
import com.bolna.clinic.entity.Doctor;
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
import java.util.Map;
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

    @Transactional
    public CallLog processWebhook(BolnaWebhookPayload payload) {
        String callId = payload.getCallId();
        log.info("Processing Bolna webhook for call_id={}, status={}", callId, payload.getStatus());

        // Upsert: find existing log for this call or create new
        CallLog callLog = callLogRepository.findByBolnaCallId(callId)
                .orElseGet(() -> {
                    CallLog fresh = new CallLog();
                    fresh.setBolnaCallId(callId);
                    fresh.setCreatedAt(LocalDateTime.now());
                    return fresh;
                });

        callLog.setStatus(payload.getStatus());

        // Update transcript only if present
        if (payload.getTranscript() != null) {
            callLog.setTranscript(payload.getTranscript());
        }

        // Link patient and doctor from recipient_data if not already set
        if (callLog.getPatient() == null || callLog.getDoctor() == null) {
            Map<String, String> recipientData = payload.getRecipientData();
            if (recipientData != null) {
                if (callLog.getPatient() == null) {
                    String patientIdStr = recipientData.get("patient_id");
                    if (patientIdStr != null) {
                        try {
                            patientRepository.findById(Long.parseLong(patientIdStr))
                                    .ifPresent(callLog::setPatient);
                        } catch (NumberFormatException ignored) {}
                    }
                }
                if (callLog.getDoctor() == null) {
                    String doctorIdStr = recipientData.get("doctor_id");
                    if (doctorIdStr != null) {
                        try {
                            doctorRepository.findById(Long.parseLong(doctorIdStr))
                                    .ifPresent(callLog::setDoctor);
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
        }

        // Link appointment if not already linked
        if (callLog.getAppointment() == null) {
            appointmentRepository.findByBolnaCallId(callId)
                    .ifPresent(callLog::setAppointment);
        }

        CallLog saved = callLogRepository.save(callLog);
        log.info("CallLog upserted: id={}, status={}", saved.getId(), saved.getStatus());
        return saved;
    }
}
