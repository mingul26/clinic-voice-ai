package com.bolna.clinic.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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

    private static final ZoneOffset IST = ZoneOffset.ofHoursMinutes(5, 30);

    @JsonSetter("slotTime")
    public void setSlotTime(String slotTime) {
        // If the value includes a timezone offset (e.g. +05:30 or Z), parse as OffsetDateTime
        // and convert to IST-local so it matches slots stored in the DB
        if (slotTime.contains("+") || slotTime.endsWith("Z")) {
            this.slotTime = OffsetDateTime.parse(slotTime)
                    .withOffsetSameInstant(IST)
                    .toLocalDateTime();
        } else {
            this.slotTime = LocalDateTime.parse(slotTime, FLEXIBLE);
        }
    }

    public void setSlotTime(LocalDateTime slotTime) { this.slotTime = slotTime; }

    public String getBolnaCallId() { return bolnaCallId; }
    public void setBolnaCallId(String bolnaCallId) { this.bolnaCallId = bolnaCallId; }
}
