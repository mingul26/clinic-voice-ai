package com.bolna.clinic.service;

import com.bolna.clinic.dto.OutboundCallRequest;
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

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Fires an outbound call via the Bolna API.
     *
     * @return the call_id returned by Bolna, or a mock ID when apiKey is blank
     */
    public String initiateCall(OutboundCallRequest request) {
        if (apiKey == null || apiKey.isBlank()) {
            String mockId = "mock-call-" + System.currentTimeMillis();
            log.warn("BOLNA_API_KEY not set — returning mock call id: {}", mockId);
            return mockId;
        }

        try {
            Map<String, Object> body = new HashMap<>();
            body.put("agent_id", agentId);
            body.put("recipient_phone_number", request.getPatientPhone());

            Map<String, String> userData = new HashMap<>();
            userData.put("patient_name", request.getPatientName());
            userData.put("language", request.getLanguage());
            if (request.getDoctorId() != null) {
                userData.put("doctor_id", String.valueOf(request.getDoctorId()));
            }
            body.put("user_data", userData);

            String json = objectMapper.writeValueAsString(body);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(bolnaApiUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                Map<?, ?> responseBody = objectMapper.readValue(response.body(), Map.class);
                String callId = (String) responseBody.get("call_id");
                log.info("Bolna call initiated successfully, call_id={}", callId);
                return callId;
            } else {
                log.error("Bolna API returned error {}: {}", response.statusCode(), response.body());
                throw new RuntimeException("Bolna API error: " + response.statusCode() + " — " + response.body());
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initiate Bolna call: " + e.getMessage(), e);
        }
    }
}
