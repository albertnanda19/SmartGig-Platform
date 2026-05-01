package com.smartgig.intelligence.controller;

import com.smartgig.common.constants.AppConstants;
import com.smartgig.common.exception.UnauthorizedException;
import com.smartgig.common.response.ApiResponse;
import com.smartgig.intelligence.dto.request.PricePredictionRequest;
import com.smartgig.intelligence.dto.response.MatchingScoreResponse;
import com.smartgig.intelligence.dto.response.MarketPositionResponse;
import com.smartgig.intelligence.dto.response.PricePredictionResponse;
import com.smartgig.intelligence.dto.response.ProjectRecommendationResponse;
import com.smartgig.intelligence.service.MatchingService;
import com.smartgig.intelligence.service.MarketIntelligenceService;
import com.smartgig.intelligence.service.PricePredictionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/intelligence")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Intelligence", description = "AI matching, pricing, recommendations")
public class IntelligenceController {
    private final MatchingService matchingService;
    private final PricePredictionService pricePredictionService;
    private final MarketIntelligenceService marketIntelligenceService;

    @PostMapping("/matching/freelancer/{freelancerId}/project/{projectId}")
    @Operation(summary = "Calculate matching score")
    public ResponseEntity<ApiResponse<MatchingScoreResponse>> calculateMatchingScore(
            @PathVariable Long freelancerId,
            @PathVariable Long projectId
    ) {
        return ResponseEntity.ok(ApiResponse.success(matchingService.calculateMatchingScore(freelancerId, projectId)));
    }

    @GetMapping("/project/{projectId}/best-freelancers")
    @Operation(summary = "Best freelancers for project (CLIENT)")
    public ResponseEntity<ApiResponse<List<MatchingScoreResponse>>> bestFreelancers(
            HttpServletRequest http,
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        validateRole(http, "CLIENT");
        return ResponseEntity.ok(ApiResponse.success(matchingService.getBestFreelancersForProject(projectId, limit)));
    }

    @PostMapping("/price-prediction")
    @Operation(summary = "Predict price (CLIENT)")
    public ResponseEntity<ApiResponse<PricePredictionResponse>> pricePrediction(HttpServletRequest http, @Valid @RequestBody PricePredictionRequest request) {
        validateRole(http, "CLIENT");
        return ResponseEntity.ok(ApiResponse.success(pricePredictionService.predictPrice(request)));
    }

    @GetMapping("/market-position")
    @Operation(summary = "Get my market position (FREELANCER)")
    public ResponseEntity<ApiResponse<MarketPositionResponse>> marketPosition(HttpServletRequest http) {
        validateRole(http, "FREELANCER");
        Long userId = extractUserId(http);
        return ResponseEntity.ok(ApiResponse.success(marketIntelligenceService.getMarketPosition(userId)));
    }

    @GetMapping("/recommendations")
    @Operation(summary = "Get project recommendations (FREELANCER)")
    public ResponseEntity<ApiResponse<List<ProjectRecommendationResponse>>> recommendations(
            HttpServletRequest http,
            @RequestParam(defaultValue = "10") int limit
    ) {
        validateRole(http, "FREELANCER");
        Long userId = extractUserId(http);
        return ResponseEntity.ok(ApiResponse.success(marketIntelligenceService.getProjectRecommendations(userId, limit)));
    }

    private Long extractUserId(HttpServletRequest request) {
        String raw = request.getHeader(AppConstants.USER_ID_HEADER);
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return Long.parseLong(raw);
    }

    private void validateRole(HttpServletRequest request, String expectedRole) {
        String role = request.getHeader(AppConstants.USER_ROLE_HEADER);
        if (!expectedRole.equals(role)) {
            throw new UnauthorizedException("This action requires " + expectedRole + " role");
        }
    }
}

