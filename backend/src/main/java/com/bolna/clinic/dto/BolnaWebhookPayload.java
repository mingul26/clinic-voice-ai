package com.bolna.clinic.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BolnaWebhookPayload {

    // Bolna sends the call identifier as "id"
    @JsonProperty("id")
    private String id;

    private String status;

    private String transcript;

    @JsonProperty("context_details")
    private ContextDetails contextDetails;

    public String getCallId() {
        return id;
    }
    public void setId(String id) { this.id = id; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTranscript() { return transcript; }
    public void setTranscript(String transcript) { this.transcript = transcript; }

    public ContextDetails getContextDetails() { return contextDetails; }
    public void setContextDetails(ContextDetails contextDetails) { this.contextDetails = contextDetails; }

    // Helpers used by CallLogService
    public String getPatientName() {
        return getRecipientData() != null ? getRecipientData().get("patient_name") : null;
    }

    public String getDoctorPreference() {
        return getRecipientData() != null ? getRecipientData().get("doctor_name") : null;
    }

    public String getSlotPreference() {
        return getRecipientData() != null ? getRecipientData().get("slot_preference") : null;
    }

    public Map<String, String> getRecipientData() {
        return contextDetails != null ? contextDetails.getRecipientData() : null;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContextDetails {
        @JsonProperty("recipient_data")
        private Map<String, String> recipientData;

        public Map<String, String> getRecipientData() { return recipientData; }
        public void setRecipientData(Map<String, String> recipientData) { this.recipientData = recipientData; }
    }
}
