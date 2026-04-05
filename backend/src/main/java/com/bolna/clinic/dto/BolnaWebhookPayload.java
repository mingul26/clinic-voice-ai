package com.bolna.clinic.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class BolnaWebhookPayload {

    @JsonProperty("call_id")
    private String callId;

    private String status;

    private Map<String, String> variables;

    private String transcript;

    public String getCallId() { return callId; }
    public void setCallId(String callId) { this.callId = callId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Map<String, String> getVariables() { return variables; }
    public void setVariables(Map<String, String> variables) { this.variables = variables; }

    public String getTranscript() { return transcript; }
    public void setTranscript(String transcript) { this.transcript = transcript; }

    public String getPatientName() {
        return variables != null ? variables.get("patient_name") : null;
    }

    public String getDoctorPreference() {
        return variables != null ? variables.get("doctor_preference") : null;
    }

    public String getSlotPreference() {
        return variables != null ? variables.get("slot_preference") : null;
    }
}
