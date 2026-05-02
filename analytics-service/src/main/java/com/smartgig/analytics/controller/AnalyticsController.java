package com.smartgig.analytics.controller;

import com.smartgig.analytics.document.ProjectAnalytics;
import com.smartgig.analytics.document.SkillTrend;
import com.smartgig.analytics.repository.ProjectAnalyticsRepository;
import com.smartgig.analytics.repository.SkillTrendRepository;
import com.smartgig.analytics.service.TrendAnalysisService;
import com.smartgig.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Analytics", description = "Reports and trends")
public class AnalyticsController {
    private final SkillTrendRepository skillTrendRepository;
    private final ProjectAnalyticsRepository projectAnalyticsRepository;
    private final TrendAnalysisService trendAnalysisService;

    @GetMapping("/skill-trends")
    @Operation(summary = "Get weekly skill trends")
    public ResponseEntity<ApiResponse<List<SkillTrend>>> skillTrends(@RequestParam String weekLabel) {
        return ResponseEntity.ok(ApiResponse.success(skillTrendRepository.findByWeekLabelOrderByRankAsc(weekLabel)));
    }

    @GetMapping("/project")
    @Operation(summary = "Get latest project analytics")
    public ResponseEntity<ApiResponse<ProjectAnalytics>> latestProjectAnalytics() {
        return ResponseEntity.ok(ApiResponse.success(projectAnalyticsRepository.findFirstByOrderByCalculatedAtDesc().orElse(null)));
    }

    @PostMapping("/recalculate")
    @Operation(summary = "Recalculate analytics now")
    public ResponseEntity<ApiResponse<Void>> recalc() {
        trendAnalysisService.calculateWeeklySkillTrends();
        trendAnalysisService.calculateProjectAnalytics();
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

