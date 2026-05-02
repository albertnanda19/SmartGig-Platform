package com.smartgig.gateway.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    private final WebClient webClient;
    private final List<String> dependencyHealthUrls;

    public HealthController(
            WebClient.Builder webClientBuilder,
            @Value("${health.full.dependencies:http://localhost:8761/actuator/health,http://localhost:8081/actuator/health,http://localhost:8082/actuator/health,http://localhost:8083/actuator/health,http://localhost:8084/actuator/health,http://localhost:8085/actuator/health,http://localhost:8086/actuator/health}") String dependencyHealthUrls
    ) {
        this.webClient = webClientBuilder.build();
        this.dependencyHealthUrls = List.of(dependencyHealthUrls.split("\\s*,\\s*"));
    }

    @GetMapping("/full")
    public Mono<ResponseEntity<Map<String, Object>>> full() {
        return Mono
                .zip(this.dependencyHealthUrls.stream().map(this::fetchHealth).toList(), results -> results)
                .map(results -> {
                    Map<String, Object> body = new LinkedHashMap<>();
                    body.put("status", allUp(results) ? "UP" : "DOWN");
                    body.put("timestamp", Instant.now().toString());

                    Map<String, Object> deps = new LinkedHashMap<>();
                    for (Object r : results) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> one = (Map<String, Object>) r;
                        deps.put((String) one.get("name"), one);
                    }
                    body.put("dependencies", deps);

                    return new ResponseEntity<>(body, allUp(results) ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE);
                });
    }

    private Mono<Map<String, Object>> fetchHealth(String url) {
        return this.webClient
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(Map.class)
                .map(raw -> Map.<String, Object>of(
                        "name", url,
                        "status", String.valueOf(raw.getOrDefault("status", "UNKNOWN"))
                ))
                .onErrorReturn(Map.of("name", url, "status", "DOWN"));
    }

    private boolean allUp(Object[] results) {
        for (Object r : results) {
            @SuppressWarnings("unchecked")
            Map<String, Object> one = (Map<String, Object>) r;
            if (!"UP".equalsIgnoreCase(String.valueOf(one.get("status")))) {
                return false;
            }
        }
        return true;
    }
}

