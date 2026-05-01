package com.smartgig.project.mapper;

import com.smartgig.project.dto.request.CreateProjectRequest;
import com.smartgig.project.dto.response.ProjectDetailResponse;
import com.smartgig.project.dto.response.ProjectResponse;
import com.smartgig.project.entity.Project;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-01T22:16:42+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 25.0.2 (Homebrew)"
)
@Component
public class ProjectMapperImpl implements ProjectMapper {

    @Override
    public Project toEntity(CreateProjectRequest request) {
        if ( request == null ) {
            return null;
        }

        Project.ProjectBuilder project = Project.builder();

        project.title( request.getTitle() );
        project.description( request.getDescription() );
        project.category( request.getCategory() );
        project.budgetMin( request.getBudgetMin() );
        project.budgetMax( request.getBudgetMax() );
        project.budgetType( request.getBudgetType() );
        project.deadline( request.getDeadline() );
        project.estimatedDurationDays( request.getEstimatedDurationDays() );
        project.complexityLevel( request.getComplexityLevel() );
        project.maxApplicants( request.getMaxApplicants() );

        return project.build();
    }

    @Override
    public ProjectResponse toResponse(Project entity) {
        if ( entity == null ) {
            return null;
        }

        ProjectResponse.ProjectResponseBuilder projectResponse = ProjectResponse.builder();

        projectResponse.id( entity.getId() );
        projectResponse.clientId( entity.getClientId() );
        projectResponse.title( entity.getTitle() );
        projectResponse.slug( entity.getSlug() );
        projectResponse.category( entity.getCategory() );
        projectResponse.budgetMin( entity.getBudgetMin() );
        projectResponse.budgetMax( entity.getBudgetMax() );
        projectResponse.budgetType( entity.getBudgetType() );
        projectResponse.complexityLevel( entity.getComplexityLevel() );
        projectResponse.currentApplicantCount( entity.getCurrentApplicantCount() );
        projectResponse.maxApplicants( entity.getMaxApplicants() );
        projectResponse.featured( entity.getFeatured() );
        projectResponse.viewsCount( entity.getViewsCount() );
        projectResponse.createdAt( entity.getCreatedAt() );
        projectResponse.updatedAt( entity.getUpdatedAt() );

        projectResponse.status( entity.getStatus().name() );

        return projectResponse.build();
    }

    @Override
    public ProjectDetailResponse toDetail(Project entity) {
        if ( entity == null ) {
            return null;
        }

        ProjectDetailResponse.ProjectDetailResponseBuilder projectDetailResponse = ProjectDetailResponse.builder();

        projectDetailResponse.id( entity.getId() );
        projectDetailResponse.clientId( entity.getClientId() );
        projectDetailResponse.title( entity.getTitle() );
        projectDetailResponse.slug( entity.getSlug() );
        projectDetailResponse.description( entity.getDescription() );
        projectDetailResponse.category( entity.getCategory() );
        projectDetailResponse.budgetMin( entity.getBudgetMin() );
        projectDetailResponse.budgetMax( entity.getBudgetMax() );
        projectDetailResponse.budgetType( entity.getBudgetType() );
        projectDetailResponse.deadline( entity.getDeadline() );
        projectDetailResponse.estimatedDurationDays( entity.getEstimatedDurationDays() );
        projectDetailResponse.complexityLevel( entity.getComplexityLevel() );
        projectDetailResponse.maxApplicants( entity.getMaxApplicants() );
        projectDetailResponse.currentApplicantCount( entity.getCurrentApplicantCount() );
        projectDetailResponse.selectedFreelancerId( entity.getSelectedFreelancerId() );
        projectDetailResponse.viewsCount( entity.getViewsCount() );
        projectDetailResponse.featured( entity.getFeatured() );
        projectDetailResponse.createdAt( entity.getCreatedAt() );
        projectDetailResponse.updatedAt( entity.getUpdatedAt() );
        projectDetailResponse.startedAt( entity.getStartedAt() );
        projectDetailResponse.completedAt( entity.getCompletedAt() );
        projectDetailResponse.cancelledAt( entity.getCancelledAt() );
        projectDetailResponse.cancellationReason( entity.getCancellationReason() );

        projectDetailResponse.status( entity.getStatus().name() );

        return projectDetailResponse.build();
    }
}
