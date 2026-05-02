package com.smartgig.e2e;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class E2ETestBase {

    protected record AuthSession(Long userId, String username, String role, String accessToken) {
    }

    protected RequestSpecification spec;

    @BeforeAll
    void setUpBase() {
        String baseUrl = firstNonBlank(
                System.getProperty("smartgig.baseUrl"),
                System.getenv("SMARTGIG_BASE_URL"),
                "http://localhost:8080"
        );

        RestAssured.baseURI = baseUrl;
        this.spec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .log(LogDetail.URI)
                .build();
    }

    protected AuthSession register(String role) {
        String nonce = Instant.now().toEpochMilli() + "-" + UUID.randomUUID().toString().substring(0, 8);
        String username = ("e2e_" + role.toLowerCase() + "_" + nonce).replace("-", "_");
        String email = username + "@example.com";
        String password = "Password1";
        String fullName = "E2E " + role + " " + nonce;

        Map<String, Object> req = new LinkedHashMap<>();
        req.put("username", username);
        req.put("email", email);
        req.put("password", password);
        req.put("role", role);
        req.put("fullName", fullName);

        var res = given()
                .spec(spec)
                .body(req)
                .when()
                .post("/api/v1/auth/register")
                .then()
                .log().ifValidationFails()
                .statusCode(201)
                .extract();

        Number userIdRaw = res.path("data.userId");
        Long userId = userIdRaw == null ? null : userIdRaw.longValue();
        String accessToken = res.path("data.accessToken");
        String returnedRole = res.path("data.role");
        String returnedUsername = res.path("data.username");
        return new AuthSession(userId, returnedUsername, returnedRole, accessToken);
    }

    protected void createProfile(AuthSession session) {
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("userId", session.userId());
        req.put("username", session.username());
        req.put("email", session.username() + "@example.com");
        req.put("fullName", "Profile " + session.username());
        req.put("role", session.role());
        req.put("bio", "Bio " + session.username());

        given()
                .spec(spec)
                .header("Authorization", "Bearer " + session.accessToken())
                .body(req)
                .when()
                .post("/api/v1/users/profile")
                .then()
                .log().ifValidationFails()
                .statusCode(201);
    }

    protected Long createProject(AuthSession client) {
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("title", "E2E Project " + UUID.randomUUID().toString().substring(0, 8));
        req.put("description", "E2E description");
        req.put("category", "SOFTWARE");
        req.put("budgetMin", 100);
        req.put("budgetMax", 250);
        req.put("publishImmediately", true);

        var res = given()
                .spec(spec)
                .header("Authorization", "Bearer " + client.accessToken())
                .body(req)
                .when()
                .post("/api/v1/projects")
                .then()
                .log().ifValidationFails()
                .statusCode(201)
                .extract();

        return res.path("data.id");
    }

    protected Long applyToProject(AuthSession freelancer, Long projectId) {
        Map<String, Object> req = new LinkedHashMap<>();
        req.put("coverLetter", "I can do this");
        req.put("proposedBudget", 200);
        req.put("estimatedDurationDays", 7);
        req.put("freelancerUsername", freelancer.username());

        var res = given()
                .spec(spec)
                .header("Authorization", "Bearer " + freelancer.accessToken())
                .body(req)
                .when()
                .post("/api/v1/projects/" + projectId + "/applications")
                .then()
                .log().ifValidationFails()
                .statusCode(201)
                .extract();

        return res.path("data.id");
    }

    private static String firstNonBlank(String... candidates) {
        for (String c : candidates) {
            if (c != null && !c.isBlank()) {
                return c;
            }
        }
        return null;
    }
}

