package com.smartgig.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateProjectRequest {
    @NotBlank
    @Size(max = 200)
    private String title;

    @NotBlank
    private String description;

    @NotBlank
    private String category;

    @NotNull
    private BigDecimal budgetMin;

    @NotNull
    private BigDecimal budgetMax;

    private String budgetType;
    private LocalDateTime deadline;
    private Integer estimatedDurationDays;
    private String complexityLevel;
    private Integer maxApplicants;
    private Boolean featured;
    private Boolean publishImmediately;

    private List<ProjectSkillRequirementItem> skills;

    @Data
    public static class ProjectSkillRequirementItem {
        @NotNull
        private Long skillId;
        @NotBlank
        private String skillName;
        private Boolean required;
    }
}

