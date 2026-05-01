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
public class ProjectStatusChangedEvent {
    private String eventId;
    private Long projectId;
    private Long clientId;
    private String previousStatus;
    private String newStatus;
    private Long affectedFreelancerId;
    private LocalDateTime changedAt;
}

