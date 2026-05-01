package com.smartgig.intelligence.service.impl;

import com.smartgig.common.exception.BusinessException;
import com.smartgig.common.exception.ResourceNotFoundException;
import com.smartgig.intelligence.client.ProjectServiceClient;
import com.smartgig.intelligence.client.UserServiceClient;
import com.smartgig.intelligence.dto.response.MatchingScoreResponse;
import com.smartgig.intelligence.dto.response.ProjectDetailResponse;
import com.smartgig.intelligence.dto.response.UserProfileResponse;
import com.smartgig.intelligence.dto.response.UserSkillResponse;
import com.smartgig.intelligence.engine.SkillMatchingEngine;
import com.smartgig.intelligence.service.MatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchingServiceImpl implements MatchingService {
    private final UserServiceClient userServiceClient;
    private final ProjectServiceClient projectServiceClient;
    private final SkillMatchingEngine skillMatchingEngine;

    @Override
    @Cacheable(value = "matchingScores", key = "#freelancerId + ':' + #projectId")
    public MatchingScoreResponse calculateMatchingScore(Long freelancerId, Long projectId) {
        UserProfileResponse profile = requireUserProfile(freelancerId);
        List<UserSkillResponse> skills = requireUserSkills(freelancerId);
        ProjectDetailResponse project = requireProject(projectId);

        Map<String, Double> freelancerSkillScores = skills.stream()
                .filter(s -> s.getSkill() != null && s.getSkill().getName() != null)
                .collect(Collectors.toMap(
                        s -> s.getSkill().getName(),
                        s -> proficiencyScore(s.getProficiencyLevel()),
                        Double::max
                ));

        List<String> required = project.getRequiredSkills() == null ? List.of() :
                project.getRequiredSkills().stream().filter(r -> Boolean.TRUE.equals(r.getRequired())).map(ProjectDetailResponse.ProjectSkillRequirementResponse::getSkillName).filter(Objects::nonNull).toList();
        List<String> optional = project.getRequiredSkills() == null ? List.of() :
                project.getRequiredSkills().stream().filter(r -> !Boolean.TRUE.equals(r.getRequired())).map(ProjectDetailResponse.ProjectSkillRequirementResponse::getSkillName).filter(Objects::nonNull).toList();

        SkillMatchingEngine.MatchingScore score = skillMatchingEngine.calculate(freelancerSkillScores, required, optional);

        return MatchingScoreResponse.builder()
                .freelancerId(freelancerId)
                .freelancerUsername(profile.getUsername())
                .projectId(projectId)
                .projectTitle(project.getTitle())
                .overallScore(score.overallScore())
                .requiredSkillsScore(score.requiredSkillsScore())
                .optionalSkillsScore(score.optionalSkillsScore())
                .matchLevel(score.matchLevel())
                .matchedSkills(score.matchedRequiredSkills())
                .missingSkills(score.missingRequiredSkills())
                .recommendation(recommendation(score.overallScore()))
                .calculatedAt(LocalDateTime.now(ZoneOffset.UTC))
                .build();
    }

    @Override
    @Cacheable(value = "bestFreelancers", key = "'project:' + #projectId + ':' + #limit")
    public List<MatchingScoreResponse> getBestFreelancersForProject(Long projectId, int limit) {
        ProjectDetailResponse project = requireProject(projectId);
        Long primarySkillId = project.getRequiredSkills() == null ? null :
                project.getRequiredSkills().stream()
                        .filter(r -> Boolean.TRUE.equals(r.getRequired()))
                        .map(ProjectDetailResponse.ProjectSkillRequirementResponse::getSkillId)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(null);

        var freelancersRes = userServiceClient.getFreelancers(primarySkillId, 0, 20);
        List<Long> candidateUserIds = freelancersRes != null && freelancersRes.isSuccess() && freelancersRes.getData() != null && freelancersRes.getData().getContent() != null
                ? freelancersRes.getData().getContent().stream().map(UserProfileResponse::getUserId).filter(Objects::nonNull).toList()
                : List.of();

        List<CompletableFuture<MatchingScoreResponse>> futures = candidateUserIds.stream()
                .map(fid -> calculateMatchingScoreAsync(fid, projectId))
                .toList();

        List<MatchingScoreResponse> results = futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingDouble(MatchingScoreResponse::getOverallScore).reversed())
                .limit(Math.max(1, limit))
                .toList();

        return results;
    }

    @Async("intelligenceTaskExecutor")
    public CompletableFuture<MatchingScoreResponse> calculateMatchingScoreAsync(Long freelancerId, Long projectId) {
        try {
            return CompletableFuture.completedFuture(calculateMatchingScore(freelancerId, projectId));
        } catch (Exception e) {
            return CompletableFuture.completedFuture(null);
        }
    }

    private UserProfileResponse requireUserProfile(Long userId) {
        var res = userServiceClient.getUserProfile(userId);
        if (res == null || !res.isSuccess() || res.getData() == null) {
            throw new ResourceNotFoundException("User profile not found");
        }
        return res.getData();
    }

    private List<UserSkillResponse> requireUserSkills(Long userId) {
        var res = userServiceClient.getUserSkills(userId);
        if (res == null || !res.isSuccess() || res.getData() == null) {
            throw new ResourceNotFoundException("User skills not found");
        }
        return res.getData();
    }

    private ProjectDetailResponse requireProject(Long projectId) {
        var res = projectServiceClient.getProject(projectId);
        if (res == null || !res.isSuccess() || res.getData() == null) {
            throw new ResourceNotFoundException("Project not found");
        }
        return res.getData();
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

    private String recommendation(double score) {
        if (score >= 0.8) return "HIGHLY_RECOMMENDED";
        if (score >= 0.6) return "RECOMMENDED";
        if (score >= 0.4) return "POSSIBLE";
        return "NOT_RECOMMENDED";
    }
}

