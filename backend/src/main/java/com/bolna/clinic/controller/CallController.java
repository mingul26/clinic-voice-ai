package com.bolna.clinic.controller;

import com.bolna.clinic.dto.OutboundCallRequest;
import com.bolna.clinic.service.BolnaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/calls")
public class CallController {

    private final BolnaService bolnaService;

    public CallController(BolnaService bolnaService) {
        this.bolnaService = bolnaService;
    }

    @PostMapping("/outbound")
    public ResponseEntity<Map<String, String>> triggerOutboundCall(
            @Valid @RequestBody OutboundCallRequest request) {
        String callId = bolnaService.initiateCall(request);
        return ResponseEntity.ok(Map.of(
                "callId", callId,
                "message", "Outbound call initiated successfully"
        ));
    }
}
