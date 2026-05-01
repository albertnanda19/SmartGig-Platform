package com.smartgig.intelligence.client;

import com.smartgig.common.response.ApiResponse;
import com.smartgig.intelligence.config.FeignConfig;
import com.smartgig.intelligence.dto.response.ProjectDetailResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "project-service",
        configuration = FeignConfig.class,
        fallback = com.smartgig.intelligence.client.fallback.ProjectServiceClientFallback.class
)
public interface ProjectServiceClient {

    @GetMapping("/api/v1/projects/{projectId}")
    ApiResponse<ProjectDetailResponse> getProject(@PathVariable Long projectId);

    @GetMapping("/api/v1/projects")
    ApiResponse<SpringPageResponse<ProjectSummaryResponse>> getProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    );
}

