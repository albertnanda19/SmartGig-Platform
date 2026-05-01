package com.smartgig.intelligence.client;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ProjectSummaryResponse {
    private Long id;
    private Long clientId;
    private String title;
    private String category;
    private String status;
    private double budgetMin;
    private double budgetMax;
    private LocalDateTime createdAt;
    private int viewsCount;
    private int currentApplicantCount;
    private int maxApplicants;
    private LocalDateTime deadline;
}

