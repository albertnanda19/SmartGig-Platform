package com.smartgig.project.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ProjectResponse {
    private Long id;
    private Long clientId;
    private String title;
    private String slug;
    private String category;
    private String status;
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private String budgetType;
    private String complexityLevel;
    private Integer currentApplicantCount;
    private Integer maxApplicants;
    private Boolean featured;
    private Integer viewsCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

