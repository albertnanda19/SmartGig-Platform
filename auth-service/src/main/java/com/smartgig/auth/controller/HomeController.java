package com.smartgig.auth.controller;

import com.smartgig.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<ApiResponse<Map<String, Object>>> home() {
        return ResponseEntity.ok(ApiResponse.success(Map.of(
            "service", "Auth Service",
            "version", "1.0.0",
            "status", "UP",
            "timestamp", LocalDateTime.now().toString(),
            "endpoints", "/api/v1/auth/**, /swagger-ui.html, /actuator/health"
        )));
    }

    @GetMapping("/actuator/health")
    public ResponseEntity<ApiResponse<Map<String, String>>> health() {
        return ResponseEntity.ok(ApiResponse.success(Map.of("status", "UP")));
    }
}
