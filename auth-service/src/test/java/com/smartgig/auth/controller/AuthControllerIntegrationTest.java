package com.smartgig.auth.controller;

import com.smartgig.auth.dto.request.LoginRequest;
import com.smartgig.auth.dto.request.RefreshTokenRequest;
import com.smartgig.auth.dto.request.RegisterRequest;
import com.smartgig.auth.dto.response.AuthResponse;
import com.smartgig.auth.entity.UserCredential;
import com.smartgig.common.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7.2-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        registry.add("spring.data.redis.password", () -> "");
        registry.add("jwt.secret", () -> "SmartGigSecretKey2024VeryLongSecretKeyForHS256Algorithm");
        registry.add("jwt.expiration", () -> "86400000");
        registry.add("jwt.refresh-expiration", () -> "604800000");
    }

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void whenRegisterWithValidData_thenReturn201() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("user_one");
        req.setEmail("user_one@example.com");
        req.setPassword("Password1");
        req.setRole(UserCredential.Role.FREELANCER);
        req.setFullName("User One");

        ResponseEntity<ApiResponse<AuthResponse>> res = restTemplate.exchange(
                "/api/v1/auth/register",
                HttpMethod.POST,
                new HttpEntity<>(req),
                new ParameterizedTypeReference<>() {}
        );

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(res.getBody()).isNotNull();
        assertThat(res.getBody().isSuccess()).isTrue();
        assertThat(res.getBody().getData()).isNotNull();
        assertThat(res.getBody().getData().getAccessToken()).isNotBlank();
        assertThat(res.getBody().getData().getRefreshToken()).isNotBlank();
    }

    @Test
    void whenRegisterWithDuplicateEmail_thenReturn400() {
        RegisterRequest req1 = new RegisterRequest();
        req1.setUsername("user_two");
        req1.setEmail("dup@example.com");
        req1.setPassword("Password1");
        req1.setRole(UserCredential.Role.CLIENT);
        req1.setFullName("User Two");

        RegisterRequest req2 = new RegisterRequest();
        req2.setUsername("user_three");
        req2.setEmail("dup@example.com");
        req2.setPassword("Password1");
        req2.setRole(UserCredential.Role.CLIENT);
        req2.setFullName("User Three");

        restTemplate.postForEntity("/api/v1/auth/register", req1, String.class);
        ResponseEntity<String> res = restTemplate.postForEntity("/api/v1/auth/register", req2, String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void whenLoginWithValidCredentials_thenReturnTokens() {
        RegisterRequest reg = new RegisterRequest();
        reg.setUsername("user_four");
        reg.setEmail("user_four@example.com");
        reg.setPassword("Password1");
        reg.setRole(UserCredential.Role.FREELANCER);
        reg.setFullName("User Four");
        restTemplate.postForEntity("/api/v1/auth/register", reg, String.class);

        LoginRequest login = new LoginRequest();
        login.setEmail("user_four@example.com");
        login.setPassword("Password1");

        ResponseEntity<ApiResponse<AuthResponse>> res = restTemplate.exchange(
                "/api/v1/auth/login",
                HttpMethod.POST,
                new HttpEntity<>(login),
                new ParameterizedTypeReference<>() {}
        );

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).isNotNull();
        assertThat(res.getBody().getData()).isNotNull();
        assertThat(res.getBody().getData().getAccessToken()).isNotBlank();
        assertThat(res.getBody().getData().getRefreshToken()).isNotBlank();
    }

    @Test
    void whenLoginWithInvalidPassword_thenReturn401() {
        RegisterRequest reg = new RegisterRequest();
        reg.setUsername("user_five");
        reg.setEmail("user_five@example.com");
        reg.setPassword("Password1");
        reg.setRole(UserCredential.Role.FREELANCER);
        reg.setFullName("User Five");
        restTemplate.postForEntity("/api/v1/auth/register", reg, String.class);

        LoginRequest login = new LoginRequest();
        login.setEmail("user_five@example.com");
        login.setPassword("Password2");

        ResponseEntity<String> res = restTemplate.postForEntity("/api/v1/auth/login", login, String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void whenRefreshWithValidToken_thenReturnNewAccessToken() {
        RegisterRequest reg = new RegisterRequest();
        reg.setUsername("user_six");
        reg.setEmail("user_six@example.com");
        reg.setPassword("Password1");
        reg.setRole(UserCredential.Role.FREELANCER);
        reg.setFullName("User Six");

        ResponseEntity<ApiResponse<AuthResponse>> regRes = restTemplate.exchange(
                "/api/v1/auth/register",
                HttpMethod.POST,
                new HttpEntity<>(reg),
                new ParameterizedTypeReference<>() {}
        );
        String refresh = regRes.getBody().getData().getRefreshToken();
        String oldAccess = regRes.getBody().getData().getAccessToken();

        RefreshTokenRequest req = new RefreshTokenRequest();
        req.setRefreshToken(refresh);

        ResponseEntity<ApiResponse<AuthResponse>> res = restTemplate.exchange(
                "/api/v1/auth/refresh",
                HttpMethod.POST,
                new HttpEntity<>(req),
                new ParameterizedTypeReference<>() {}
        );

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).isNotNull();
        assertThat(res.getBody().getData()).isNotNull();
        assertThat(res.getBody().getData().getAccessToken()).isNotBlank();
        assertThat(res.getBody().getData().getAccessToken()).isNotEqualTo(oldAccess);
    }

    @Test
    void whenAccessProtectedEndpointWithoutToken_thenReturn401() {
        ResponseEntity<String> res = restTemplate.postForEntity("/api/v1/auth/logout", null, String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}

