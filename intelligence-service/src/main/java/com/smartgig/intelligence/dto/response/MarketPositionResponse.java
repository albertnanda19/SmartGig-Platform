package com.smartgig.intelligence.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MarketPositionResponse {
    private Long freelancerId;
    private int percentileRank;
    private String rateCompetitiveness;
    private List<String> topDemandedSkills;
    private List<SkillGapItem> skillsToImprove;
    private Double estimatedMonthlyEarningMin;
    private Double estimatedMonthlyEarningMax;
    private String overallAssessment;

    @Data
    @Builder
    public static class SkillGapItem {
        private String skillName;
        private double relevanceScore;
        private String reason;
    }
}

