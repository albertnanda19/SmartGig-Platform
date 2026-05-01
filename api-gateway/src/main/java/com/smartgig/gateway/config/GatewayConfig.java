package com.smartgig.gateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartgig.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Configuration
@RequiredArgsConstructor
public class GatewayConfig {

    @Bean
    @Order(-2)
    public ErrorWebExceptionHandler errorWebExceptionHandler(ObjectMapper objectMapper) {
        return (ServerWebExchange exchange, Throwable ex) -> {
            if (exchange.getResponse().isCommitted()) {
                return Mono.error(ex);
            }

            HttpStatus status = statusOf(ex);
            exchange.getResponse().setStatusCode(status);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            byte[] bytes;
            try {
                bytes = objectMapper.writeValueAsBytes(ApiResponse.error(messageOf(ex, status)));
            } catch (Exception e) {
                bytes = "{\"success\":false,\"message\":\"Internal server error\"}".getBytes(StandardCharsets.UTF_8);
            }
            return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
        };
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    private HttpStatus statusOf(Throwable ex) {
        if (ex instanceof ResponseStatusException rse) {
            return HttpStatus.valueOf(rse.getStatusCode().value());
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String messageOf(Throwable ex, HttpStatus status) {
        if (status == HttpStatus.TOO_MANY_REQUESTS) {
            return "Rate limit exceeded";
        }
        if (ex instanceof ResponseStatusException rse && rse.getReason() != null && !rse.getReason().isBlank()) {
            return rse.getReason();
        }
        return status.is5xxServerError() ? "Internal server error" : "Request failed";
    }
}

