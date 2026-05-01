package com.smartgig.user.mapper;

import com.smartgig.user.dto.response.SkillResponse;
import com.smartgig.user.entity.Skill;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SkillMapper {
    @Mapping(target = "category", expression = "java(entity.getCategory().name())")
    @Mapping(target = "distance", ignore = true)
    SkillResponse toResponse(Skill entity);
}

