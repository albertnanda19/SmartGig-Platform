package com.smartgig.gateway.controller;

import com.smartgig.common.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Mono<ResponseEntity<ApiResponse<Map<String, Object>>>> home() {
        Map<String, Object> data = new HashMap<>();
        data.put("service", "API Gateway");
        data.put("version", "1.0.0");
        data.put("status", "UP");
        data.put("timestamp", Instant.now().toString());
        data.put("routes",
                "/api/v1/auth/**, /api/v1/users/**, /api/v1/projects/**, /api/v1/intelligence/**, /actuator/health");
        return Mono.just(ResponseEntity.ok(ApiResponse.success(data)));
    }

    @GetMapping("/actuator/health")
    public Mono<ResponseEntity<ApiResponse<Map<String, String>>>> health() {
        return Mono.just(ResponseEntity.ok(ApiResponse.success(Map.of("status", "UP"))));
    }
}
