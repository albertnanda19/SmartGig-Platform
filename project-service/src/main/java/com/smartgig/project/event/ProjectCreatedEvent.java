package com.smartgig.project.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectCreatedEvent {
    private String eventId;
    private Long projectId;
    private Long clientId;
    private String title;
    private String category;
    private List<String> requiredSkills;
    private Double budgetMin;
    private Double budgetMax;
    private LocalDateTime createdAt;
}

