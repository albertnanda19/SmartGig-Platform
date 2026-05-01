package com.smartgig.project.mapper;

import com.smartgig.project.dto.response.ProjectApplicationResponse;
import com.smartgig.project.entity.ProjectApplication;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-01T22:16:42+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 25.0.2 (Homebrew)"
)
@Component
public class ProjectApplicationMapperImpl implements ProjectApplicationMapper {

    @Override
    public ProjectApplicationResponse toResponse(ProjectApplication entity) {
        if ( entity == null ) {
            return null;
        }

        ProjectApplicationResponse.ProjectApplicationResponseBuilder projectApplicationResponse = ProjectApplicationResponse.builder();

        projectApplicationResponse.id( entity.getId() );
        projectApplicationResponse.freelancerId( entity.getFreelancerId() );
        projectApplicationResponse.freelancerUsername( entity.getFreelancerUsername() );
        projectApplicationResponse.coverLetter( entity.getCoverLetter() );
        projectApplicationResponse.proposedBudget( entity.getProposedBudget() );
        projectApplicationResponse.estimatedDurationDays( entity.getEstimatedDurationDays() );
        projectApplicationResponse.status( entity.getStatus() );
        projectApplicationResponse.matchingScore( entity.getMatchingScore() );
        projectApplicationResponse.clientNotes( entity.getClientNotes() );
        projectApplicationResponse.appliedAt( entity.getAppliedAt() );
        projectApplicationResponse.reviewedAt( entity.getReviewedAt() );

        projectApplicationResponse.projectId( entity.getProject().getId() );

        return projectApplicationResponse.build();
    }
}
