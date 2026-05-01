package com.smartgig.project.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectAppliedEvent {
    private String eventId;
    private Long projectId;
    private Long freelancerId;
    private String freelancerUsername;
    private Long applicationId;
    private Double proposedBudget;
    private LocalDateTime appliedAt;
}

