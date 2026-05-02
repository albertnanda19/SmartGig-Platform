package com.smartgig.user.integration;

import com.smartgig.common.constants.AppConstants;
import com.smartgig.common.response.ApiResponse;
import com.smartgig.user.dto.request.CreateUserProfileRequest;
import com.smartgig.user.dto.response.UserProfileResponse;
import com.smartgig.user.entity.UserProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("User profile integration tests")
class UserProfileIntegrationTest extends BaseIntegrationTest {

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    @DisplayName("Create profile then fetch it by userId")
    void createAndFetchProfile() {
        CreateUserProfileRequest req = new CreateUserProfileRequest();
        req.setUserId(101L);
        req.setUsername("user_one");
        req.setEmail("user_one@example.com");
        req.setFullName("User One");
        req.setRole(UserProfile.UserRole.FREELANCER);

        HttpHeaders headers = new HttpHeaders();
        headers.set(AppConstants.USER_ID_HEADER, "101");

        ResponseEntity<ApiResponse<UserProfileResponse>> createRes = restTemplate.exchange(
                "/api/v1/users/profile",
                HttpMethod.POST,
                new HttpEntity<>(req, headers),
                new ParameterizedTypeReference<>() {
                }
        );

        assertThat(createRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createRes.getBody()).isNotNull();
        assertThat(createRes.getBody().getData()).isNotNull();
        assertThat(createRes.getBody().getData().getUserId()).isEqualTo(101L);

        ResponseEntity<ApiResponse<UserProfileResponse>> getRes = restTemplate.exchange(
                "/api/v1/users/101/profile",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {
                }
        );

        assertThat(getRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getRes.getBody()).isNotNull();
        assertThat(getRes.getBody().getData()).isNotNull();
        assertThat(getRes.getBody().getData().getEmail()).isEqualTo("user_one@example.com");
    }
}

