package com.smartgig.project.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ProjectDetailResponse {
    private Long id;
    private Long clientId;
    private String title;
    private String slug;
    private String description;
    private String category;
    private String status;
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private String budgetType;
    private LocalDateTime deadline;
    private Integer estimatedDurationDays;
    private String complexityLevel;
    private Integer maxApplicants;
    private Integer currentApplicantCount;
    private Long selectedFreelancerId;
    private Integer viewsCount;
    private Boolean featured;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    private List<ProjectSkillRequirementResponse> requiredSkills;

    @Data
    @Builder
    public static class ProjectSkillRequirementResponse {
        private Long skillId;
        private String skillName;
        private Boolean required;
    }
}

