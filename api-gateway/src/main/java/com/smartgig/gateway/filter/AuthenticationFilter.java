package com.smartgig.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartgig.common.constants.AppConstants;
import com.smartgig.common.response.ApiResponse;
import com.smartgig.gateway.util.JwtUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final List<String> WHITELIST = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh",
            "/api/v1/auth/",
            "/api/v1/projects/",
            "/api/v1/users/",
            "/actuator/**");

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuthenticationFilter(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();
            if (isWhitelisted(path)) {
                return chain.filter(exchange);
            }

            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith(AppConstants.JWT_PREFIX)) {
                return unauthorized(exchange, "Missing or invalid Authorization header");
            }

            String token = authHeader.substring(AppConstants.JWT_PREFIX.length());
            if (!jwtUtil.validateToken(token)) {
                return unauthorized(exchange, "Invalid or expired token");
            }

            Long userId = jwtUtil.extractUserId(token);
            String email = jwtUtil.extractEmail(token);
            String role = jwtUtil.extractRole(token);
            String username = jwtUtil.extractUsername(token);
            String traceId = UUID.randomUUID().toString();

            ServerWebExchange mutated = exchange.mutate()
                    .request(r -> r.headers(h -> {
                        if (userId != null) {
                            h.set(AppConstants.USER_ID_HEADER, String.valueOf(userId));
                        }
                        if (email != null) {
                            h.set(AppConstants.USER_EMAIL_HEADER, email);
                        }
                        if (role != null) {
                            h.set(AppConstants.USER_ROLE_HEADER, role);
                        }
                        if (username != null) {
                            h.set("X-Username", username);
                        }
                        h.set(AppConstants.TRACE_ID_HEADER, traceId);
                    }))
                    .build();

            return chain.filter(mutated);
        };
    }

    private boolean isWhitelisted(String path) {
        return WHITELIST.stream().anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] bytes = serialize(ApiResponse.error(message));
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
    }

    private byte[] serialize(Object obj) {
        try {
            return objectMapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            String fallback = "{\"success\":false,\"message\":\"Internal server error\"}";
            return fallback.getBytes(StandardCharsets.UTF_8);
        }
    }

    @Data
    public static class Config {
    }
}
