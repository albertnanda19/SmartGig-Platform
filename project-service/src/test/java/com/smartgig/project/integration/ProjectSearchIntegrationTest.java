package com.smartgig.project.integration;

import com.smartgig.common.constants.AppConstants;
import com.smartgig.common.response.ApiResponse;
import com.smartgig.project.dto.request.CreateProjectRequest;
import com.smartgig.project.dto.response.ProjectResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Project search integration tests")
class ProjectSearchIntegrationTest extends BaseIntegrationTest {

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("Create OPEN project then search by keyword returns results")
    void createAndSearch() throws Exception {
        CreateProjectRequest req = new CreateProjectRequest();
        req.setTitle("Elasticsearch Java Integration");
        req.setDescription("Build a search feature with Elasticsearch");
        req.setCategory("SEARCH");
        req.setBudgetMin(new BigDecimal("1000"));
        req.setBudgetMax(new BigDecimal("2000"));
        req.setPublishImmediately(true);

        HttpHeaders headers = new HttpHeaders();
        headers.set(AppConstants.USER_ID_HEADER, "201");
        headers.set(AppConstants.USER_ROLE_HEADER, "CLIENT");

        ResponseEntity<ApiResponse<ProjectResponse>> createRes = restTemplate.exchange(
                "/api/v1/projects",
                HttpMethod.POST,
                new HttpEntity<>(req, headers),
                new ParameterizedTypeReference<>() {
                }
        );

        assertThat(createRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createRes.getBody()).isNotNull();
        assertThat(createRes.getBody().getData()).isNotNull();

        ResponseEntity<String> searchRes = restTemplate.exchange(
                "/api/v1/projects/search?keyword=Elasticsearch&page=0&size=10",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        assertThat(searchRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(searchRes.getBody()).isNotNull();

        JsonNode root = objectMapper.readTree(searchRes.getBody());
        JsonNode content = root.path("data").path("content");
        assertThat(content.isArray()).isTrue();
        assertThat(content.size()).isGreaterThan(0);
    }
}

