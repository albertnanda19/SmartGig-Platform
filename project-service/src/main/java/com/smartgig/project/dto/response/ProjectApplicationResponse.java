package com.smartgig.project.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ProjectApplicationResponse {
    private Long id;
    private Long projectId;
    private Long freelancerId;
    private String freelancerUsername;
    private String coverLetter;
    private BigDecimal proposedBudget;
    private Integer estimatedDurationDays;
    private String status;
    private BigDecimal matchingScore;
    private String clientNotes;
    private LocalDateTime appliedAt;
    private LocalDateTime reviewedAt;
}

