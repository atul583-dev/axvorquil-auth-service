package com.axvorquil.auth.controller;

import com.axvorquil.auth.dto.ApiResponse;
import com.axvorquil.auth.dto.SystemHealthDto;
import com.axvorquil.auth.service.HealthAggregatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminHealthController {

    private final HealthAggregatorService healthService;

    /** Aggregate health status of all microservices */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<SystemHealthDto>> health() {
        return ResponseEntity.ok(ApiResponse.ok(healthService.aggregate()));
    }
}
