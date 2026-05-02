package com.smartgig.project.controller;

import com.smartgig.common.constants.AppConstants;
import com.smartgig.common.exception.UnauthorizedException;
import com.smartgig.common.response.ApiResponse;
import com.smartgig.project.document.ProjectDocument;
import com.smartgig.project.dto.request.CreateProjectRequest;
import com.smartgig.project.dto.request.ProjectSearchRequest;
import com.smartgig.project.dto.request.UpdateProjectRequest;
import com.smartgig.project.dto.request.UpdateProjectStatusRequest;
import com.smartgig.project.dto.response.ProjectDetailResponse;
import com.smartgig.project.dto.response.ProjectResponse;
import com.smartgig.project.service.ProjectSearchService;
import com.smartgig.project.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Projects", description = "Project marketplace")
public class ProjectController {
    private final ProjectService projectService;
    private final ProjectSearchService projectSearchService;

    @GetMapping("/")
    @Operation(summary = "Project service root endpoint")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> getProjectsRoot() {
        return ResponseEntity.ok(ApiResponse.success(java.util.Map.of(
                "service", "Project Service",
                "version", "1.0.0",
                "endpoints", "/api/v1/projects, /api/v1/projects/{id}, /api/v1/projects/search/**",
                "status", "UP")));
    }

    @PostMapping
    @Operation(summary = "Create project (CLIENT)")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(HttpServletRequest http,
            @Valid @RequestBody CreateProjectRequest request) {
        validateRole(http, "CLIENT");
        Long clientId = extractUserId(http);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Created", projectService.createProject(clientId, request)));
    }

    @GetMapping
    @Operation(summary = "Get all projects")
    public ResponseEntity<ApiResponse<Page<ProjectResponse>>> getAllProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(projectService.getAllProjects(pageable)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search projects (Elasticsearch)")
    public ResponseEntity<ApiResponse<Page<ProjectDocument>>> searchProjects(
            @ModelAttribute ProjectSearchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(projectSearchService.searchProjects(request)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project detail")
    public ResponseEntity<ApiResponse<ProjectDetailResponse>> getProjectDetail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(projectService.getProjectDetail(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update project (CLIENT owner)")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(HttpServletRequest http, @PathVariable Long id,
            @RequestBody UpdateProjectRequest request) {
        validateRole(http, "CLIENT");
        Long clientId = extractUserId(http);
        return ResponseEntity.ok(ApiResponse.success("Updated", projectService.updateProject(id, clientId, request)));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update project status via state machine (CLIENT owner)")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateStatus(HttpServletRequest http, @PathVariable Long id,
            @RequestBody UpdateProjectStatusRequest request) {
        validateRole(http, "CLIENT");
        Long clientId = extractUserId(http);
        return ResponseEntity
                .ok(ApiResponse.success("Updated", projectService.updateProjectStatus(id, clientId, request)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete project (CLIENT owner, only DRAFT)")
    public ResponseEntity<ApiResponse<Void>> deleteProject(HttpServletRequest http, @PathVariable Long id) {
        validateRole(http, "CLIENT");
        Long clientId = extractUserId(http);
        projectService.deleteProject(id, clientId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private Long extractUserId(HttpServletRequest request) {
        String raw = request.getHeader(AppConstants.USER_ID_HEADER);
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return Long.parseLong(raw);
    }

    private void validateRole(HttpServletRequest request, String expectedRole) {
        String role = request.getHeader(AppConstants.USER_ROLE_HEADER);
        if (!expectedRole.equals(role)) {
            throw new UnauthorizedException("This action requires " + expectedRole + " role");
        }
    }
}
