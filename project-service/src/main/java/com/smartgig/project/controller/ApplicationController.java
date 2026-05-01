package com.smartgig.project.controller;

import com.smartgig.common.constants.AppConstants;
import com.smartgig.common.exception.UnauthorizedException;
import com.smartgig.common.response.ApiResponse;
import com.smartgig.project.dto.request.ApplyToProjectRequest;
import com.smartgig.project.dto.response.ProjectApplicationResponse;
import com.smartgig.project.service.ProjectApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/applications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Applications", description = "Project applications")
public class ApplicationController {
    private final ProjectApplicationService applicationService;

    @PostMapping
    @Operation(summary = "Apply to a project (FREELANCER)")
    public ResponseEntity<ApiResponse<ProjectApplicationResponse>> applyToProject(
            HttpServletRequest http,
            @PathVariable Long projectId,
            @Valid @RequestBody ApplyToProjectRequest request
    ) {
        validateRole(http, "FREELANCER");
        Long freelancerId = extractUserId(http);
        ProjectApplicationResponse res = applicationService.applyToProject(projectId, freelancerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Applied", res));
    }

    @GetMapping
    @Operation(summary = "Get applications for a project (CLIENT owner)")
    public ResponseEntity<ApiResponse<List<ProjectApplicationResponse>>> getApplications(HttpServletRequest http, @PathVariable Long projectId) {
        validateRole(http, "CLIENT");
        Long clientId = extractUserId(http);
        return ResponseEntity.ok(ApiResponse.success(applicationService.getApplications(projectId, clientId)));
    }

    @GetMapping("/my")
    @Operation(summary = "Get my applications (FREELANCER)")
    public ResponseEntity<ApiResponse<List<ProjectApplicationResponse>>> getMyApplications(HttpServletRequest http, @PathVariable Long projectId) {
        validateRole(http, "FREELANCER");
        Long freelancerId = extractUserId(http);
        return ResponseEntity.ok(ApiResponse.success(applicationService.getMyApplications(freelancerId)));
    }

    @PatchMapping("/{applicationId}/status")
    @Operation(summary = "Update application status (CLIENT owner)")
    public ResponseEntity<ApiResponse<ProjectApplicationResponse>> updateApplicationStatus(
            HttpServletRequest http,
            @PathVariable Long projectId,
            @PathVariable Long applicationId,
            @RequestParam String status
    ) {
        validateRole(http, "CLIENT");
        Long clientId = extractUserId(http);
        return ResponseEntity.ok(ApiResponse.success("Updated", applicationService.updateApplicationStatus(projectId, applicationId, clientId, status)));
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

