package com.smartgig.user.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SkillGapAnalysisResponse {
    private Long userId;
    private List<SkillGapItem> suggestedSkills;
    private int currentSkillCount;
    private String profileStrength;

    @Data
    @Builder
    public static class SkillGapItem {
        private Long skillId;
        private String skillName;
        private String category;
        private double relevanceScore;
        private String reason;
        private String relatedExistingSkill;
        private String relationType;
    }
}

