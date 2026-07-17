package com.fuelflex.platform.common.controller;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("status", "UP");
        response.put("application", "FuelFlex Platform");
        response.put("message", "FuelFlex backend is operational");
        response.put("timestamp", OffsetDateTime.now());

        return ResponseEntity.ok(response);
    }
}
