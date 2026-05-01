package com.smartgig.project.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UpdateProjectRequest {
    private String title;
    private String description;
    private String category;
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private String budgetType;
    private LocalDateTime deadline;
    private Integer estimatedDurationDays;
    private String complexityLevel;
    private Integer maxApplicants;
    private Boolean featured;
    private List<CreateProjectRequest.ProjectSkillRequirementItem> skills;
}

