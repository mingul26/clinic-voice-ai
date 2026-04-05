package com.bolna.clinic.service;

import com.bolna.clinic.dto.OutboundCallRequest;
import com.bolna.clinic.entity.Doctor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@Service
public class BolnaService {

    private static final Logger log = LoggerFactory.getLogger(BolnaService.class);

    @Value("${bolna.api.key}")
    private String apiKey;

    @Value("${bolna.agent.id}")
    private String agentId;

    @Value("${bolna.api.url}")
    private String bolnaApiUrl;

    /**
     * The verified phone number registered in Bolna account.
     * For demo, all outbound calls ring this number regardless of patient phone.
     */
    @Value("${bolna.verified.number:}")
    private String verifiedNumber;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(10))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String initiateCall(OutboundCallRequest request, Doctor doctor) {
        if (apiKey == null || apiKey.isBlank()) {
            String mockId = "mock-call-" + System.currentTimeMillis();
            log.warn("BOLNA_API_KEY not set — returning mock call id: {}", mockId);
            return mockId;
        }

        try {
            // Use verified number for demo; fall back to patient phone if not set
            String recipientPhone = (verifiedNumber != null && !verifiedNumber.isBlank())
                    ? verifiedNumber
                    : request.getPatientPhone();

            Map<String, Object> body = new HashMap<>();
            body.put("agent_id", agentId);
            body.put("recipient_phone_number", recipientPhone);

            Map<String, String> variables = new HashMap<>();
            variables.put("patient_name", request.getPatientName());
            variables.put("patient_id", String.valueOf(request.getPatientId()));
            variables.put("language", request.getLanguage());
            variables.put("call_reason", toSnakeCase(request.getCallReason().name()));
            variables.put("context", request.getContext() != null ? request.getContext() : "");

            if (doctor != null) {
                variables.put("doctor_name", doctor.getName());
                variables.put("doctor_id", String.valueOf(doctor.getId()));
                variables.put("doctor_specialization", doctor.getSpecialization());
            }

            body.put("variables", variables);

            String json = objectMapper.writeValueAsString(body);
            log.info("Sending Bolna request to {}: {}", bolnaApiUrl, json);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(bolnaApiUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .timeout(java.time.Duration.ofSeconds(15))
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            log.info("Firing HTTP request to Bolna...");
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            log.info("Bolna responded with status={} body={}", response.statusCode(), response.body());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                Map<?, ?> responseBody = objectMapper.readValue(response.body(), Map.class);
                String callId = (String) responseBody.get("call_id");
                log.info("Bolna call initiated, call_id={}", callId);
                return callId;
            } else {
                log.error("Bolna API error {}: {}", response.statusCode(), response.body());
                throw new RuntimeException("Bolna API error: " + response.statusCode() + " — " + response.body());
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initiate Bolna call: " + e.getMessage(), e);
        }
    }

    private String toSnakeCase(String enumName) {
        return enumName.toLowerCase().replace('_', '_');
    }
}
