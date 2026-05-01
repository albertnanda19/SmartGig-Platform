package com.smartgig.project.mapper;

import com.smartgig.project.dto.request.CreateProjectRequest;
import com.smartgig.project.dto.response.ProjectDetailResponse;
import com.smartgig.project.dto.response.ProjectResponse;
import com.smartgig.project.entity.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "clientId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "currentApplicantCount", ignore = true)
    @Mapping(target = "selectedFreelancerId", ignore = true)
    @Mapping(target = "viewsCount", ignore = true)
    @Mapping(target = "featured", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "startedAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    @Mapping(target = "cancellationReason", ignore = true)
    Project toEntity(CreateProjectRequest request);

    @Mapping(target = "status", expression = "java(entity.getStatus().name())")
    ProjectResponse toResponse(Project entity);

    @Mapping(target = "status", expression = "java(entity.getStatus().name())")
    @Mapping(target = "requiredSkills", ignore = true)
    ProjectDetailResponse toDetail(Project entity);
}

