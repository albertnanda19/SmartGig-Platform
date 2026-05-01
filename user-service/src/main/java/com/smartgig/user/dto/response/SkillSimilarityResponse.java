package com.smartgig.user.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SkillSimilarityResponse {
    private Long userId1;
    private Long userId2;
    private double similarityScore;
    private String similarityLevel;
    private List<String> commonSkills;
    private List<String> uniqueSkillsUser1;
    private List<String> uniqueSkillsUser2;
    private List<SuggestedSkill> suggestedSkillsForUser1;

    @Data
    @Builder
    public static class SuggestedSkill {
        private Long skillId;
        private String skillName;
        private double relevanceScore;
        private String reason;
    }
}

