package com.smartgig.intelligence.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ProjectRecommendationResponse {
    private Long projectId;
    private String projectTitle;
    private String category;
    private double compositeScore;
    private double skillMatchScore;
    private double budgetAttractivenessScore;
    private List<String> whyRecommended;
    private double budgetMin;
    private double budgetMax;
    private LocalDateTime deadline;
}

