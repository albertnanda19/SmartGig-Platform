package com.smartgig.e2e;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

@DisplayName("E2E - complete freelance flow via gateway")
class CompleteFreelanceFlowTest extends E2ETestBase {

    @Test
    @DisplayName("Client posts project, freelancer applies, client sees application")
    void completeFlow() {
        AuthSession client = register("CLIENT");
        AuthSession freelancer = register("FREELANCER");

        createProfile(client);
        createProfile(freelancer);

        Long projectId = createProject(client);
        applyToProject(freelancer, projectId);

        Awaitility.await()
                .atMost(Duration.ofSeconds(20))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    List<Map<String, Object>> apps = given()
                            .spec(spec)
                            .header("Authorization", "Bearer " + client.accessToken())
                            .when()
                            .get("/api/v1/projects/" + projectId + "/applications")
                            .then()
                            .log().ifValidationFails()
                            .statusCode(200)
                            .body("data.size()", greaterThanOrEqualTo(1))
                            .extract()
                            .path("data");

                    if (apps == null || apps.isEmpty()) {
                        throw new AssertionError("No applications returned");
                    }
                });
    }
}

