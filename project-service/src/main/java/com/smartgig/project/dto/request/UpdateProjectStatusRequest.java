package com.smartgig.project.dto.request;

import com.smartgig.project.statemachine.ProjectEvent;
import lombok.Data;

@Data
public class UpdateProjectStatusRequest {
    private ProjectEvent event;
    private String reason;
    private Long freelancerId;
}

