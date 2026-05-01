package com.smartgig.intelligence.service.impl;

import com.smartgig.common.exception.ResourceNotFoundException;
import com.smartgig.intelligence.client.UserServiceClient;
import com.smartgig.intelligence.client.ProjectServiceClient;
import com.smartgig.intelligence.client.ProjectSummaryResponse;
import com.smartgig.intelligence.dto.response.MarketPositionResponse;
import com.smartgig.intelligence.dto.response.ProjectRecommendationResponse;
import com.smartgig.intelligence.dto.response.ProjectDetailResponse;
import com.smartgig.intelligence.dto.response.UserProfileResponse;
import com.smartgig.intelligence.dto.response.UserSkillResponse;
import com.smartgig.intelligence.engine.RecommendationEngine;
import com.smartgig.intelligence.engine.SkillMatchingEngine;
import com.smartgig.intelligence.engine.SkillMatchingEngine.MatchingScore;
import com.smartgig.intelligence.service.MarketIntelligenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MarketIntelligenceServiceImpl implements MarketIntelligenceService {
    private final UserServiceClient userServiceClient;
    private final ProjectServiceClient projectServiceClient;
    private final RecommendationEngine recommendationEngine;
    private final SkillMatchingEngine skillMatchingEngine;

    @Override
    @Cacheable(value = "marketPosition", key = "#freelancerId")
    public MarketPositionResponse getMarketPosition(Long freelancerId) {
        UserProfileResponse profile = requireProfile(freelancerId);
        List<UserSkillResponse> skills = requireSkills(freelancerId);

        double hourlyRate = profile.getHourlyRate() == null ? 0.0 : profile.getHourlyRate().doubleValue();
        int percentile = hourlyRate <= 0.0 ? 50 : (hourlyRate < 50 ? 35 : hourlyRate < 80 ? 55 : hourlyRate < 120 ? 75 : 90);
        String competitiveness = hourlyRate < 50 ? "BELOW_MARKET" : hourlyRate < 90 ? "AT_MARKET" : hourlyRate < 140 ? "ABOVE_MARKET" : "PREMIUM";

        List<String> topSkills = skills.stream()
                .map(s -> s.getSkill() == null ? null : s.getSkill().getName())
                .filter(Objects::nonNull)
                .limit(5)
                .toList();

        double monthlyMin = hourlyRate * 8.0 * 20.0 * 0.6;
        double monthlyMax = hourlyRate * 8.0 * 20.0 * 0.9;

        return MarketPositionResponse.builder()
                .freelancerId(freelancerId)
                .percentileRank(percentile)
                .rateCompetitiveness(competitiveness)
                .topDemandedSkills(topSkills)
                .skillsToImprove(List.of())
                .estimatedMonthlyEarningMin(monthlyMin)
                .estimatedMonthlyEarningMax(monthlyMax)
                .overallAssessment(percentile >= 75 ? "STRONG_POSITION" : percentile >= 50 ? "MODERATE_POSITION" : "WEAK_POSITION")
                .build();
    }

    @Override
    @Cacheable(value = "recommendations", key = "#freelancerId + ':' + #limit")
    public List<ProjectRecommendationResponse> getProjectRecommendations(Long freelancerId, int limit) {
        UserProfileResponse profile = requireProfile(freelancerId);
        List<UserSkillResponse> skills = requireSkills(freelancerId);

        Map<String, Double> freelancerSkillScores = skills.stream()
                .filter(s -> s.getSkill() != null && s.getSkill().getName() != null)
                .collect(Collectors.toMap(
                        s -> s.getSkill().getName(),
                        s -> proficiencyScore(s.getProficiencyLevel()),
                        Double::max
                ));

        var projectsRes = projectServiceClient.getProjects(0, 50);
        List<ProjectSummaryResponse> projects = projectsRes != null && projectsRes.isSuccess() && projectsRes.getData() != null
                ? safeList(projectsRes.getData().getContent())
                : List.of();

        RecommendationEngine.FreelancerScoringContext freelancerCtx = new RecommendationEngine.FreelancerScoringContext(
                freelancerId,
                profile.getHourlyRate() == null ? 0.0 : profile.getHourlyRate().doubleValue(),
                profile.getYearsOfExperience() == null ? 0 : profile.getYearsOfExperience()
        );

        List<ProjectRecommendationResponse> recommendations = new ArrayList<>();
        for (ProjectSummaryResponse p : projects) {
            if (p == null || p.getId() == null) {
                continue;
            }
            if (p.getStatus() != null && !p.getStatus().equalsIgnoreCase("OPEN")) {
                continue;
            }

            var detailRes = projectServiceClient.getProject(p.getId());
            if (detailRes == null || !detailRes.isSuccess() || detailRes.getData() == null) {
                continue;
            }

            var detail = detailRes.getData();
            List<String> required = detail.getRequiredSkills() == null ? List.of() :
                    detail.getRequiredSkills().stream().filter(r -> Boolean.TRUE.equals(r.getRequired())).map(ProjectDetailResponse.ProjectSkillRequirementResponse::getSkillName).filter(Objects::nonNull).toList();
            List<String> optional = detail.getRequiredSkills() == null ? List.of() :
                    detail.getRequiredSkills().stream().filter(r -> !Boolean.TRUE.equals(r.getRequired())).map(ProjectDetailResponse.ProjectSkillRequirementResponse::getSkillName).filter(Objects::nonNull).toList();

            MatchingScore skillScore = skillMatchingEngine.calculate(freelancerSkillScores, required, optional);

            RecommendationEngine.ProjectScoringContext projectCtx = new RecommendationEngine.ProjectScoringContext(
                    p.getId(),
                    p.getBudgetMin(),
                    p.getBudgetMax(),
                    p.getCreatedAt() == null ? LocalDateTime.now(ZoneOffset.UTC) : p.getCreatedAt(),
                    p.getViewsCount(),
                    p.getCurrentApplicantCount(),
                    p.getMaxApplicants()
            );

            double composite = recommendationEngine.scoreProjectForFreelancer(projectCtx, freelancerCtx, skillScore);

            double budgetMid = (p.getBudgetMin() + p.getBudgetMax()) / 2.0;
            double expectedDaily = freelancerCtx.hourlyRate() * 8.0;
            double budgetAttractiveness = expectedDaily <= 0 ? 0.5 : Math.max(0.0, 1.0 - Math.abs(budgetMid - expectedDaily) / expectedDaily);

            List<String> why = new ArrayList<>();
            why.add("Skill match " + skillScore.matchLevel());
            if (budgetAttractiveness >= 0.7) why.add("Budget fits your rate");
            if (p.getViewsCount() < 50) why.add("Low competition");

            recommendations.add(ProjectRecommendationResponse.builder()
                    .projectId(p.getId())
                    .projectTitle(p.getTitle())
                    .category(p.getCategory())
                    .compositeScore(composite)
                    .skillMatchScore(skillScore.overallScore())
                    .budgetAttractivenessScore(Math.min(1.0, Math.max(0.0, budgetAttractiveness)))
                    .whyRecommended(why)
                    .budgetMin(p.getBudgetMin())
                    .budgetMax(p.getBudgetMax())
                    .deadline(p.getDeadline())
                    .build());
        }

        return recommendations.stream()
                .sorted((a, b) -> Double.compare(b.getCompositeScore(), a.getCompositeScore()))
                .limit(Math.max(1, limit))
                .toList();
    }

    private UserProfileResponse requireProfile(Long userId) {
        var res = userServiceClient.getUserProfile(userId);
        if (res == null || !res.isSuccess() || res.getData() == null) {
            throw new ResourceNotFoundException("User profile not found");
        }
        return res.getData();
    }

    private List<UserSkillResponse> requireSkills(Long userId) {
        var res = userServiceClient.getUserSkills(userId);
        if (res == null || !res.isSuccess() || res.getData() == null) {
            throw new ResourceNotFoundException("User skills not found");
        }
        return res.getData();
    }

    private <T> List<T> safeList(List<T> list) {
        return list == null ? List.of() : list;
    }

    private double proficiencyScore(String level) {
        if (level == null) return 0.25;
        return switch (level.toUpperCase(Locale.ROOT)) {
            case "EXPERT" -> 1.0;
            case "ADVANCED" -> 0.75;
            case "INTERMEDIATE" -> 0.5;
            case "BEGINNER" -> 0.25;
            default -> 0.25;
        };
    }
}

