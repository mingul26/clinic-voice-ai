package com.bolna.clinic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class OutboundCallRequest {

    @NotBlank(message = "Patient phone is required")
    private String patientPhone;

    @NotBlank(message = "Patient name is required")
    private String patientName;

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    private String language = "Hindi";

    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    @NotNull(message = "Call reason is required")
    private CallReason callReason;

    private String context;

    public enum CallReason {
        REPORT_READY, RESCHEDULE, CALLBACK
    }

    public String getPatientPhone() { return patientPhone; }
    public void setPatientPhone(String patientPhone) { this.patientPhone = patientPhone; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public CallReason getCallReason() { return callReason; }
    public void setCallReason(CallReason callReason) { this.callReason = callReason; }

    public String getContext() { return context; }
    public void setContext(String context) { this.context = context; }
}
