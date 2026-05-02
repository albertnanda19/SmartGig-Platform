package com.smartgig.auth.integration;

import com.smartgig.auth.dto.request.LoginRequest;
import com.smartgig.auth.dto.request.RefreshTokenRequest;
import com.smartgig.auth.dto.request.RegisterRequest;
import com.smartgig.auth.dto.response.AuthResponse;
import com.smartgig.auth.entity.UserCredential;
import com.smartgig.common.response.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Auth flow integration tests")
class AuthFlowIntegrationTest extends BaseIntegrationTest {

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    @DisplayName("Register -> Login -> Refresh returns new access token")
    void registerLoginRefresh() {
        RegisterRequest reg = new RegisterRequest();
        reg.setUsername("user_one");
        reg.setEmail("user_one@example.com");
        reg.setPassword("Password1");
        reg.setRole(UserCredential.Role.FREELANCER);
        reg.setFullName("User One");

        ResponseEntity<ApiResponse<AuthResponse>> regRes = restTemplate.exchange(
                "/api/v1/auth/register",
                HttpMethod.POST,
                new HttpEntity<>(reg),
                new ParameterizedTypeReference<>() {
                }
        );

        assertThat(regRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(regRes.getBody()).isNotNull();
        assertThat(regRes.getBody().getData()).isNotNull();

        LoginRequest login = new LoginRequest();
        login.setEmail("user_one@example.com");
        login.setPassword("Password1");

        ResponseEntity<ApiResponse<AuthResponse>> loginRes = restTemplate.exchange(
                "/api/v1/auth/login",
                HttpMethod.POST,
                new HttpEntity<>(login),
                new ParameterizedTypeReference<>() {
                }
        );

        assertThat(loginRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginRes.getBody()).isNotNull();
        assertThat(loginRes.getBody().getData()).isNotNull();

        String refreshToken = loginRes.getBody().getData().getRefreshToken();
        String oldAccessToken = loginRes.getBody().getData().getAccessToken();

        RefreshTokenRequest refresh = new RefreshTokenRequest();
        refresh.setRefreshToken(refreshToken);

        ResponseEntity<ApiResponse<AuthResponse>> refreshRes = restTemplate.exchange(
                "/api/v1/auth/refresh",
                HttpMethod.POST,
                new HttpEntity<>(refresh),
                new ParameterizedTypeReference<>() {
                }
        );

        assertThat(refreshRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(refreshRes.getBody()).isNotNull();
        assertThat(refreshRes.getBody().getData()).isNotNull();
        assertThat(refreshRes.getBody().getData().getAccessToken()).isNotBlank();
        assertThat(refreshRes.getBody().getData().getAccessToken()).isNotEqualTo(oldAccessToken);
    }
}

