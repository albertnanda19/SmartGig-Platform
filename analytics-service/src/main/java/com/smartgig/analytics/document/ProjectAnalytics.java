package com.smartgig.analytics.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "project_analytics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectAnalytics {
    @Id
    private String id;

    private String periodLabel;
    private String periodType;
    private int totalProjectsCreated;
    private int totalProjectsCompleted;
    private int totalProjectsCancelled;
    private double avgBudget;
    private double totalValueCompleted;
    private Map<String, Integer> projectsByCategory;
    private Map<String, Double> avgBudgetByCategory;
    private int totalApplications;
    private double avgApplicationsPerProject;

    @CreatedDate
    private LocalDateTime calculatedAt;
}

