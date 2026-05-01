package com.smartgig.intelligence.client.fallback;

import com.smartgig.common.response.ApiResponse;
import com.smartgig.intelligence.client.ProjectServiceClient;
import com.smartgig.intelligence.client.ProjectSummaryResponse;
import com.smartgig.intelligence.client.SpringPageResponse;
import com.smartgig.intelligence.dto.response.ProjectDetailResponse;
import org.springframework.stereotype.Component;

@Component
public class ProjectServiceClientFallback implements ProjectServiceClient {
    @Override
    public ApiResponse<ProjectDetailResponse> getProject(Long projectId) {
        return ApiResponse.error("Project service unavailable");
    }

    @Override
    public ApiResponse<SpringPageResponse<ProjectSummaryResponse>> getProjects(int page, int size) {
        return ApiResponse.error("Project service unavailable");
    }
}

