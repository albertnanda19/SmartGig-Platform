package com.smartgig.project.service.impl;

import com.smartgig.common.exception.BusinessException;
import com.smartgig.common.exception.ResourceNotFoundException;
import com.smartgig.project.dto.request.ApplyToProjectRequest;
import com.smartgig.project.dto.response.ProjectApplicationResponse;
import com.smartgig.project.entity.Project;
import com.smartgig.project.entity.ProjectApplication;
import com.smartgig.project.event.ProjectAppliedEvent;
import com.smartgig.project.mapper.ProjectApplicationMapper;
import com.smartgig.project.repository.ProjectApplicationRepository;
import com.smartgig.project.repository.ProjectRepository;
import com.smartgig.project.service.OutboxService;
import com.smartgig.project.service.ProjectApplicationService;
import com.smartgig.project.statemachine.ProjectEvent;
import com.smartgig.project.statemachine.ProjectStateMachineService;
import com.smartgig.project.statemachine.ProjectStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectApplicationServiceImpl implements ProjectApplicationService {
    private final ProjectRepository projectRepository;
    private final ProjectApplicationRepository applicationRepository;
    private final ProjectApplicationMapper mapper;
    private final ProjectStateMachineService stateMachineService;
    private final OutboxService outboxService;

    @Override
    @Transactional
    public ProjectApplicationResponse applyToProject(Long projectId, Long freelancerId, ApplyToProjectRequest request) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        if (project.getStatus() != ProjectStatus.OPEN) {
            throw new BusinessException("Project is not open");
        }
        if (project.getCurrentApplicantCount() != null && project.getMaxApplicants() != null && project.getCurrentApplicantCount() >= project.getMaxApplicants()) {
            throw new BusinessException("Project is full");
        }
        if (applicationRepository.existsByProjectIdAndFreelancerId(projectId, freelancerId)) {
            throw new BusinessException("Already applied");
        }
        if (project.getClientId().equals(freelancerId)) {
            throw new BusinessException("Cannot apply to own project");
        }

        validateProposedBudget(project, request.getProposedBudget());

        ProjectApplication saved = applicationRepository.save(ProjectApplication.builder()
                .project(project)
                .freelancerId(freelancerId)
                .freelancerUsername(request.getFreelancerUsername())
                .coverLetter(request.getCoverLetter())
                .proposedBudget(request.getProposedBudget())
                .estimatedDurationDays(request.getEstimatedDurationDays())
                .status("PENDING")
                .appliedAt(LocalDateTime.now(ZoneOffset.UTC))
                .build());

        project.setCurrentApplicantCount((project.getCurrentApplicantCount() == null ? 0 : project.getCurrentApplicantCount()) + 1);
        ProjectStatus next = stateMachineService.sendEvent(project, ProjectEvent.RECEIVE_APPLICATION);
        project.setStatus(next);
        project.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
        projectRepository.save(project);

        ProjectAppliedEvent evt = ProjectAppliedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .projectId(project.getId())
                .freelancerId(freelancerId)
                .freelancerUsername(request.getFreelancerUsername())
                .applicationId(saved.getId())
                .proposedBudget(saved.getProposedBudget().doubleValue())
                .appliedAt(saved.getAppliedAt())
                .build();
        outboxService.enqueue("project.applied", String.valueOf(project.getId()), evt);

        return mapper.toResponse(saved);
    }

    @Override
    public List<ProjectApplicationResponse> getApplications(Long projectId, Long clientId) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        if (!project.getClientId().equals(clientId)) {
            throw new BusinessException("Not project owner");
        }
        return applicationRepository.findByProjectId(projectId).stream().map(mapper::toResponse).toList();
    }

    @Override
    public List<ProjectApplicationResponse> getMyApplications(Long freelancerId) {
        return applicationRepository.findByFreelancerId(freelancerId).stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional
    public ProjectApplicationResponse updateApplicationStatus(Long projectId, Long applicationId, Long clientId, String status) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        if (!project.getClientId().equals(clientId)) {
            throw new BusinessException("Not project owner");
        }
        ProjectApplication app = applicationRepository.findByIdAndProjectId(applicationId, projectId).orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        app.setStatus(status);
        app.setReviewedAt(LocalDateTime.now(ZoneOffset.UTC));
        return mapper.toResponse(applicationRepository.save(app));
    }

    private void validateProposedBudget(Project project, BigDecimal proposed) {
        BigDecimal min = project.getBudgetMin().multiply(new BigDecimal("0.8"));
        BigDecimal max = project.getBudgetMax().multiply(new BigDecimal("1.2"));
        if (proposed.compareTo(min) < 0 || proposed.compareTo(max) > 0) {
            throw new BusinessException("Proposed budget out of acceptable range");
        }
    }
}

