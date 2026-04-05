package com.bolna.clinic.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

public class AppointmentBookRequest {

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    @NotNull(message = "Slot time is required")
    private LocalDateTime slotTime;

    private String bolnaCallId;

    private static final DateTimeFormatter FLEXIBLE = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd'T'HH:mm")
            .optionalStart().appendPattern(":ss").optionalEnd()
            .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
            .toFormatter();

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public LocalDateTime getSlotTime() { return slotTime; }

    @JsonSetter("slotTime")
    public void setSlotTime(String slotTime) {
        this.slotTime = LocalDateTime.parse(slotTime, FLEXIBLE);
    }

    public void setSlotTime(LocalDateTime slotTime) { this.slotTime = slotTime; }

    public String getBolnaCallId() { return bolnaCallId; }
    public void setBolnaCallId(String bolnaCallId) { this.bolnaCallId = bolnaCallId; }
}
