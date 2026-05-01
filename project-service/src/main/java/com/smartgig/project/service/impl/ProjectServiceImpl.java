package com.smartgig.project.service.impl;

import com.smartgig.common.exception.BusinessException;
import com.smartgig.common.exception.ResourceNotFoundException;
import com.smartgig.project.document.ProjectDocument;
import com.smartgig.project.dto.request.CreateProjectRequest;
import com.smartgig.project.dto.request.UpdateProjectRequest;
import com.smartgig.project.dto.request.UpdateProjectStatusRequest;
import com.smartgig.project.dto.response.ProjectDetailResponse;
import com.smartgig.project.dto.response.ProjectDetailResponse.ProjectSkillRequirementResponse;
import com.smartgig.project.dto.response.ProjectResponse;
import com.smartgig.project.entity.Project;
import com.smartgig.project.entity.ProjectSkillRequirement;
import com.smartgig.project.event.ProjectCreatedEvent;
import com.smartgig.project.event.ProjectStatusChangedEvent;
import com.smartgig.project.mapper.ProjectMapper;
import com.smartgig.project.repository.ProjectRepository;
import com.smartgig.project.repository.ProjectSkillRequirementRepository;
import com.smartgig.project.repository.search.ProjectSearchRepository;
import com.smartgig.project.service.OutboxService;
import com.smartgig.project.service.ProjectService;
import com.smartgig.project.statemachine.ProjectEvent;
import com.smartgig.project.statemachine.ProjectStateMachineService;
import com.smartgig.project.statemachine.ProjectStatus;
import com.smartgig.common.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectSkillRequirementRepository requirementRepository;
    private final ProjectMapper projectMapper;
    private final ProjectStateMachineService stateMachineService;
    private final ProjectSearchRepository searchRepository;
    private final OutboxService outboxService;

    @Override
    @Transactional
    public ProjectResponse createProject(Long clientId, CreateProjectRequest request) {
        validateBudget(request.getBudgetMin(), request.getBudgetMax());

        Project project = projectMapper.toEntity(request);
        project.setClientId(clientId);
        project.setStatus(ProjectStatus.DRAFT);
        project.setBudgetType(request.getBudgetType() == null ? "FIXED" : request.getBudgetType());
        project.setComplexityLevel(request.getComplexityLevel() == null ? "MEDIUM" : request.getComplexityLevel());
        project.setMaxApplicants(request.getMaxApplicants() == null ? 10 : request.getMaxApplicants());
        project.setCurrentApplicantCount(0);
        project.setViewsCount(0);
        project.setFeatured(request.getFeatured() != null && request.getFeatured());
        project.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
        project.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));

        String baseSlug = SlugUtil.toSlug(project.getTitle());
        String slug = uniqueSlug(baseSlug);
        project.setSlug(slug);

        Project saved = projectRepository.save(project);

        if (request.getSkills() != null) {
            for (CreateProjectRequest.ProjectSkillRequirementItem s : request.getSkills()) {
                requirementRepository.save(ProjectSkillRequirement.builder()
                        .project(saved)
                        .skillId(s.getSkillId())
                        .skillName(s.getSkillName())
                        .required(s.getRequired() == null || s.getRequired())
                        .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                        .build());
            }
        }

        if (Boolean.TRUE.equals(request.getPublishImmediately())) {
            ProjectStatus newStatus = stateMachineService.sendEvent(saved, ProjectEvent.PUBLISH);
            saved.setStatus(newStatus);
            saved.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
            saved = projectRepository.save(saved);
        }

        index(saved);

        ProjectCreatedEvent event = ProjectCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .projectId(saved.getId())
                .clientId(saved.getClientId())
                .title(saved.getTitle())
                .category(saved.getCategory())
                .requiredSkills(requirementRepository.findByProjectId(saved.getId()).stream().map(ProjectSkillRequirement::getSkillName).toList())
                .budgetMin(saved.getBudgetMin().doubleValue())
                .budgetMax(saved.getBudgetMax().doubleValue())
                .createdAt(saved.getCreatedAt())
                .build();
        outboxService.enqueue("project.created", String.valueOf(saved.getId()), event);

        return projectMapper.toResponse(saved);
    }

    @Override
    public org.springframework.data.domain.Page<ProjectResponse> getAllProjects(org.springframework.data.domain.Pageable pageable) {
        return projectRepository.findAll(pageable).map(projectMapper::toResponse);
    }

    @Override
    @Transactional
    public ProjectDetailResponse getProjectDetail(Long projectId) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        incrementViewsAsync(project.getId());
        ProjectDetailResponse detail = projectMapper.toDetail(project);
        List<ProjectSkillRequirementResponse> skills = requirementRepository.findByProjectId(projectId).stream()
                .map(r -> ProjectSkillRequirementResponse.builder()
                        .skillId(r.getSkillId())
                        .skillName(r.getSkillName())
                        .required(r.getRequired())
                        .build())
                .toList();
        detail.setRequiredSkills(skills);
        return detail;
    }

    @Async
    @Transactional
    public void incrementViewsAsync(Long projectId) {
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null) {
            return;
        }
        project.setViewsCount((project.getViewsCount() == null ? 0 : project.getViewsCount()) + 1);
        project.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
        projectRepository.save(project);
        index(project);
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(Long projectId, Long clientId, UpdateProjectRequest request) {
        Project project = projectRepository.findByIdAndClientId(projectId, clientId).orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        if (request.getBudgetMin() != null || request.getBudgetMax() != null) {
            validateBudget(request.getBudgetMin() == null ? project.getBudgetMin() : request.getBudgetMin(),
                    request.getBudgetMax() == null ? project.getBudgetMax() : request.getBudgetMax());
        }

        if (request.getTitle() != null) {
            project.setTitle(request.getTitle());
            project.setSlug(uniqueSlug(SlugUtil.toSlug(request.getTitle())));
        }
        if (request.getDescription() != null) project.setDescription(request.getDescription());
        if (request.getCategory() != null) project.setCategory(request.getCategory());
        if (request.getBudgetMin() != null) project.setBudgetMin(request.getBudgetMin());
        if (request.getBudgetMax() != null) project.setBudgetMax(request.getBudgetMax());
        if (request.getBudgetType() != null) project.setBudgetType(request.getBudgetType());
        if (request.getDeadline() != null) project.setDeadline(request.getDeadline());
        if (request.getEstimatedDurationDays() != null) project.setEstimatedDurationDays(request.getEstimatedDurationDays());
        if (request.getComplexityLevel() != null) project.setComplexityLevel(request.getComplexityLevel());
        if (request.getMaxApplicants() != null) project.setMaxApplicants(request.getMaxApplicants());
        if (request.getFeatured() != null) project.setFeatured(request.getFeatured());
        project.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));

        Project saved = projectRepository.save(project);

        if (request.getSkills() != null) {
            requirementRepository.deleteAll(requirementRepository.findByProjectId(projectId));
            for (CreateProjectRequest.ProjectSkillRequirementItem s : request.getSkills()) {
                requirementRepository.save(ProjectSkillRequirement.builder()
                        .project(saved)
                        .skillId(s.getSkillId())
                        .skillName(s.getSkillName())
                        .required(s.getRequired() == null || s.getRequired())
                        .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                        .build());
            }
        }

        index(saved);
        return projectMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ProjectResponse updateProjectStatus(Long projectId, Long clientId, UpdateProjectStatusRequest request) {
        Project project = projectRepository.findByIdAndClientId(projectId, clientId).orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        ProjectStatus prev = project.getStatus();

        if (request.getEvent() == ProjectEvent.SELECT_FREELANCER) {
            if (request.getFreelancerId() == null) {
                throw new BusinessException("freelancerId is required");
            }
            project.setSelectedFreelancerId(request.getFreelancerId());
        }

        ProjectStatus next = stateMachineService.sendEvent(project, request.getEvent());
        if (next == prev) {
            throw new BusinessException("Invalid status transition");
        }
        project.setStatus(next);
        if (request.getEvent() == ProjectEvent.CANCEL_PROJECT) {
            project.setCancellationReason(request.getReason());
        }
        project.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
        Project saved = projectRepository.save(project);

        index(saved);

        ProjectStatusChangedEvent evt = ProjectStatusChangedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .projectId(saved.getId())
                .clientId(saved.getClientId())
                .previousStatus(prev.name())
                .newStatus(saved.getStatus().name())
                .affectedFreelancerId(saved.getSelectedFreelancerId())
                .changedAt(LocalDateTime.now(ZoneOffset.UTC))
                .build();
        outboxService.enqueue("project.status.changed", String.valueOf(saved.getId()), evt);

        return projectMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteProject(Long projectId, Long clientId) {
        Project project = projectRepository.findByIdAndClientId(projectId, clientId).orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        if (project.getStatus() != ProjectStatus.DRAFT) {
            throw new BusinessException("Only DRAFT projects can be deleted");
        }
        requirementRepository.deleteAll(requirementRepository.findByProjectId(projectId));
        projectRepository.delete(project);
        searchRepository.deleteById(String.valueOf(projectId));
    }

    @Override
    @Transactional
    public void indexProject(Long projectId) {
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        index(project);
    }

    private void validateBudget(BigDecimal min, BigDecimal max) {
        if (min == null || max == null || min.compareTo(max) > 0) {
            throw new BusinessException("Invalid budget range");
        }
    }

    private String uniqueSlug(String base) {
        String slug = base == null || base.isBlank() ? UUID.randomUUID().toString() : base;
        if (!projectRepository.existsBySlug(slug)) {
            return slug;
        }
        for (int i = 0; i < 20; i++) {
            String candidate = slug + "-" + String.format("%04d", (int) (Math.random() * 10000));
            if (!projectRepository.existsBySlug(candidate)) {
                return candidate;
            }
        }
        return slug + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private void index(Project project) {
        List<String> skillNames = requirementRepository.findByProjectId(project.getId()).stream().map(ProjectSkillRequirement::getSkillName).toList();
        searchRepository.save(ProjectDocument.builder()
                .id(String.valueOf(project.getId()))
                .projectId(project.getId())
                .title(project.getTitle())
                .description(project.getDescription())
                .category(project.getCategory())
                .status(project.getStatus().name())
                .budgetMin(project.getBudgetMin().doubleValue())
                .budgetMax(project.getBudgetMax().doubleValue())
                .budgetType(project.getBudgetType())
                .skillNames(skillNames)
                .complexityLevel(project.getComplexityLevel())
                .isFeatured(project.getFeatured())
                .viewsCount(project.getViewsCount())
                .createdAt(project.getCreatedAt())
                .deadline(project.getDeadline())
                .clientId(project.getClientId())
                .build());
    }
}

