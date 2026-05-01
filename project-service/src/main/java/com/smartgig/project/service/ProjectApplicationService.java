package com.smartgig.project.service;

import com.smartgig.project.dto.request.ApplyToProjectRequest;
import com.smartgig.project.dto.response.ProjectApplicationResponse;

import java.util.List;

public interface ProjectApplicationService {
    ProjectApplicationResponse applyToProject(Long projectId, Long freelancerId, ApplyToProjectRequest request);

    List<ProjectApplicationResponse> getApplications(Long projectId, Long clientId);

    List<ProjectApplicationResponse> getMyApplications(Long freelancerId);

    ProjectApplicationResponse updateApplicationStatus(Long projectId, Long applicationId, Long clientId, String status);
}

