package com.smartgig.user.mapper;

import com.smartgig.user.dto.request.CreateUserProfileRequest;
import com.smartgig.user.dto.response.UserProfileResponse;
import com.smartgig.user.entity.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "totalProjectsCompleted", constant = "0")
    @Mapping(target = "totalEarnings", expression = "java(java.math.BigDecimal.ZERO)")
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "available", constant = "true")
    @Mapping(target = "hourlyRate", ignore = true)
    @Mapping(target = "yearsOfExperience", expression = "java(0)")
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "portfolioUrl", ignore = true)
    @Mapping(target = "linkedinUrl", ignore = true)
    @Mapping(target = "githubUrl", ignore = true)
    UserProfile toEntity(CreateUserProfileRequest request);

    @Mapping(target = "role", expression = "java(entity.getRole().name())")
    UserProfileResponse toResponse(UserProfile entity);
}

