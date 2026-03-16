package com.axvorquil.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityClient {

    private final RestTemplate restTemplate;

    @Value("${security.service.url:http://localhost:8085}")
    private String securityServiceUrl;

    /**
     * Fire-and-forget: send audit event to security service.
     * If security service is down, auth still works — we just log a warning.
     */
    public void sendEvent(String userId, String userEmail, String action,
                          String ipAddress, String userAgent, String details, String severity) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("userId",    userId);
            payload.put("userEmail", userEmail);
            payload.put("action",    action);
            payload.put("severity",  severity);
            payload.put("ipAddress", ipAddress);
            payload.put("userAgent", userAgent);
            payload.put("details",   details);

            restTemplate.postForObject(
                securityServiceUrl + "/api/security/internal/events",
                payload,
                Map.class
            );
        } catch (Exception e) {
            log.warn("Could not send audit event to security service: {}", e.getMessage());
        }
    }
}
