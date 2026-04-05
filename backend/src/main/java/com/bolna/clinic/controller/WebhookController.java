package com.bolna.clinic.controller;

import com.bolna.clinic.dto.BolnaWebhookPayload;
import com.bolna.clinic.entity.CallLog;
import com.bolna.clinic.service.CallLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/webhook")
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    private final CallLogService callLogService;

    public WebhookController(CallLogService callLogService) {
        this.callLogService = callLogService;
    }

    @PostMapping("/bolna")
    public ResponseEntity<Map<String, Object>> handleBolnaWebhook(
            @RequestBody(required = false) String rawBody) {
        log.info("Received Bolna webhook raw body: {}", rawBody);
        BolnaWebhookPayload payload;
        try {
            payload = new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(rawBody, BolnaWebhookPayload.class);
        } catch (Exception e) {
            log.error("Failed to parse webhook body: {}", e.getMessage());
            return ResponseEntity.ok(Map.of("received", true));
        }
        log.info("Received Bolna webhook: call_id={}, status={}", payload.getCallId(), payload.getStatus());
        CallLog callLog = callLogService.processWebhook(payload);
        return ResponseEntity.ok(Map.of(
                "received", true,
                "callLogId", callLog.getId()
        ));
    }
}
