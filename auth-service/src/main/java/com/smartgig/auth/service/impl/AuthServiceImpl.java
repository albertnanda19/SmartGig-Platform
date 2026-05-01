package com.smartgig.auth.service.impl;

import com.smartgig.auth.dto.request.LoginRequest;
import com.smartgig.auth.dto.request.RefreshTokenRequest;
import com.smartgig.auth.dto.request.RegisterRequest;
import com.smartgig.auth.dto.response.AuthResponse;
import com.smartgig.auth.entity.UserCredential;
import com.smartgig.auth.mapper.UserCredentialMapper;
import com.smartgig.auth.repository.UserCredentialRepository;
import com.smartgig.auth.service.AuthService;
import com.smartgig.auth.util.JwtUtil;
import com.smartgig.common.exception.BusinessException;
import com.smartgig.common.exception.UnauthorizedException;
import com.smartgig.common.response.ApiResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class AuthServiceImpl implements AuthService {
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(30);
    private static final Duration REFRESH_TTL = Duration.ofDays(7);

    private final UserCredentialRepository repository;
    private final UserCredentialMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;
    private final long accessExpirationMs;

    @Override
    @Transactional
    public ApiResponse<AuthResponse> register(RegisterRequest request) {
        if (repository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already registered");
        }
        if (repository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Username already taken");
        }

        UserCredential entity = mapper.toEntity(request);
        entity.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        UserCredential saved = repository.saveAndFlush(entity);
        saved = repository.findById(saved.getId()).orElseThrow();

        String accessToken = jwtUtil.generateAccessToken(saved.getUserId(), saved.getEmail(), saved.getRole().name(), saved.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(saved.getUserId());
        storeRefreshToken(saved.getUserId(), refreshToken);

        return ApiResponse.success("Registered", toAuthResponse(saved, accessToken, refreshToken));
    }

    @Override
    @Transactional
    public ApiResponse<AuthResponse> login(LoginRequest request) {
        UserCredential user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(now)) {
            throw new BusinessException("Account is locked");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);
            if (attempts >= MAX_FAILED_ATTEMPTS) {
                user.setLockedUntil(now.plus(LOCK_DURATION));
            }
            repository.save(user);
            throw new UnauthorizedException("Invalid credentials");
        }

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(now);
        UserCredential saved = repository.save(user);

        String accessToken = jwtUtil.generateAccessToken(saved.getUserId(), saved.getEmail(), saved.getRole().name(), saved.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(saved.getUserId());
        storeRefreshToken(saved.getUserId(), refreshToken);

        return ApiResponse.success("Logged in", toAuthResponse(saved, accessToken, refreshToken));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<AuthResponse> refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new UnauthorizedException("Refresh token expired or invalid");
        }

        Long userId = jwtUtil.extractUserId(refreshToken);
        if (userId == null) {
            throw new UnauthorizedException("Refresh token expired or invalid");
        }

        String key = refreshKey(userId);
        String stored = redisTemplate.opsForValue().get(key);
        if (stored == null || !stored.equals(refreshToken)) {
            throw new UnauthorizedException("Refresh token expired or invalid");
        }

        UserCredential user = repository.findByUserId(userId)
                .orElseThrow(() -> new UnauthorizedException("Refresh token expired or invalid"));

        String accessToken = jwtUtil.generateAccessToken(userId, user.getEmail(), user.getRole().name(), user.getUsername());
        AuthResponse response = toAuthResponse(user, accessToken, refreshToken);
        return ApiResponse.success("Refreshed", response);
    }

    @Override
    public ApiResponse<Void> logout(Long userId) {
        if (userId != null) {
            redisTemplate.delete(refreshKey(userId));
        }
        return ApiResponse.success(null);
    }

    private AuthResponse toAuthResponse(UserCredential user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(Duration.ofMillis(accessExpirationMs).toSeconds())
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    private void storeRefreshToken(Long userId, String refreshToken) {
        redisTemplate.opsForValue().set(refreshKey(userId), refreshToken, REFRESH_TTL);
    }

    private String refreshKey(Long userId) {
        return "refresh:" + userId;
    }

    public AuthServiceImpl(
            UserCredentialRepository repository,
            UserCredentialMapper mapper,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            RedisTemplate<String, String> redisTemplate,
            @org.springframework.beans.factory.annotation.Value("${jwt.expiration}") long accessExpirationMs
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
        this.accessExpirationMs = accessExpirationMs;
    }
}

