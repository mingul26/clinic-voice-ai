package com.bolna.clinic.dto;

import jakarta.validation.constraints.NotBlank;

public class OutboundCallRequest {

    @NotBlank(message = "Patient phone is required")
    private String patientPhone;

    @NotBlank(message = "Patient name is required")
    private String patientName;

    private String language = "Hindi";

    private Long doctorId;

    public String getPatientPhone() { return patientPhone; }
    public void setPatientPhone(String patientPhone) { this.patientPhone = patientPhone; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
}
