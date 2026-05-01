package com.smartgig.intelligence.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ProjectDetailResponse {
    private Long id;
    private Long clientId;
    private String title;
    private String description;
    private String category;
    private String status;
    private Double budgetMin;
    private Double budgetMax;
    private String complexityLevel;
    private Integer currentApplicantCount;
    private Integer maxApplicants;
    private Integer viewsCount;
    private Boolean featured;
    private LocalDateTime createdAt;
    private LocalDateTime deadline;
    private List<ProjectSkillRequirementResponse> requiredSkills;

    @Data
    @Builder
    public static class ProjectSkillRequirementResponse {
        private Long skillId;
        private String skillName;
        private Boolean required;
    }
}

