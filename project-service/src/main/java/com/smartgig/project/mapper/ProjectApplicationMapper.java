package com.smartgig.project.mapper;

import com.smartgig.project.dto.response.ProjectApplicationResponse;
import com.smartgig.project.entity.ProjectApplication;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectApplicationMapper {
    @Mapping(target = "projectId", expression = "java(entity.getProject().getId())")
    ProjectApplicationResponse toResponse(ProjectApplication entity);
}

