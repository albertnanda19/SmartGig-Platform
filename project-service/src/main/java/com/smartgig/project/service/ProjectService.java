package com.smartgig.project.service;

import com.smartgig.project.document.ProjectDocument;
import com.smartgig.project.dto.request.CreateProjectRequest;
import com.smartgig.project.dto.request.UpdateProjectRequest;
import com.smartgig.project.dto.request.UpdateProjectStatusRequest;
import com.smartgig.project.dto.response.ProjectDetailResponse;
import com.smartgig.project.dto.response.ProjectResponse;
import com.smartgig.project.statemachine.ProjectEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProjectService {
    ProjectResponse createProject(Long clientId, CreateProjectRequest request);

    Page<ProjectResponse> getAllProjects(Pageable pageable);

    ProjectDetailResponse getProjectDetail(Long projectId);

    ProjectResponse updateProject(Long projectId, Long clientId, UpdateProjectRequest request);

    ProjectResponse updateProjectStatus(Long projectId, Long clientId, UpdateProjectStatusRequest request);

    void deleteProject(Long projectId, Long clientId);

    void indexProject(Long projectId);
}

