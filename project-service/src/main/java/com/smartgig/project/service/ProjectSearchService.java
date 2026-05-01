package com.smartgig.project.service;

import com.smartgig.project.document.ProjectDocument;
import com.smartgig.project.dto.request.ProjectSearchRequest;
import org.springframework.data.domain.Page;

public interface ProjectSearchService {
    Page<ProjectDocument> searchProjects(ProjectSearchRequest request);
}

