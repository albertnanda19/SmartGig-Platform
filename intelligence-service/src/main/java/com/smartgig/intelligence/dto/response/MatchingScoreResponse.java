package com.smartgig.intelligence.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class MatchingScoreResponse {
    private Long freelancerId;
    private String freelancerUsername;
    private Long projectId;
    private String projectTitle;
    private double overallScore;
    private double requiredSkillsScore;
    private double optionalSkillsScore;
    private String matchLevel;
    private List<String> matchedSkills;
    private List<String> missingSkills;
    private String recommendation;
    private LocalDateTime calculatedAt;
}

