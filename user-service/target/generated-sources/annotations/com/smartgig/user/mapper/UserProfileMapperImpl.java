package com.smartgig.user.mapper;

import com.smartgig.user.dto.request.CreateUserProfileRequest;
import com.smartgig.user.dto.response.UserProfileResponse;
import com.smartgig.user.entity.UserProfile;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-02T01:47:34+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 25.0.2 (Homebrew)"
)
@Component
public class UserProfileMapperImpl implements UserProfileMapper {

    @Override
    public UserProfile toEntity(CreateUserProfileRequest request) {
        if ( request == null ) {
            return null;
        }

        UserProfile.UserProfileBuilder userProfile = UserProfile.builder();

        userProfile.userId( request.getUserId() );
        userProfile.username( request.getUsername() );
        userProfile.email( request.getEmail() );
        userProfile.fullName( request.getFullName() );
        userProfile.role( request.getRole() );
        userProfile.bio( request.getBio() );
        userProfile.avatarUrl( request.getAvatarUrl() );

        userProfile.totalProjectsCompleted( 0 );
        userProfile.totalEarnings( java.math.BigDecimal.ZERO );
        userProfile.available( true );
        userProfile.yearsOfExperience( 0 );

        return userProfile.build();
    }

    @Override
    public UserProfileResponse toResponse(UserProfile entity) {
        if ( entity == null ) {
            return null;
        }

        UserProfileResponse.UserProfileResponseBuilder userProfileResponse = UserProfileResponse.builder();

        userProfileResponse.userId( entity.getUserId() );
        userProfileResponse.username( entity.getUsername() );
        userProfileResponse.email( entity.getEmail() );
        userProfileResponse.fullName( entity.getFullName() );
        userProfileResponse.bio( entity.getBio() );
        userProfileResponse.avatarUrl( entity.getAvatarUrl() );
        userProfileResponse.hourlyRate( entity.getHourlyRate() );
        userProfileResponse.yearsOfExperience( entity.getYearsOfExperience() );
        userProfileResponse.location( entity.getLocation() );
        userProfileResponse.portfolioUrl( entity.getPortfolioUrl() );
        userProfileResponse.linkedinUrl( entity.getLinkedinUrl() );
        userProfileResponse.githubUrl( entity.getGithubUrl() );
        userProfileResponse.totalProjectsCompleted( entity.getTotalProjectsCompleted() );
        userProfileResponse.totalEarnings( entity.getTotalEarnings() );
        userProfileResponse.averageRating( entity.getAverageRating() );
        userProfileResponse.available( entity.getAvailable() );
        userProfileResponse.createdAt( entity.getCreatedAt() );
        userProfileResponse.updatedAt( entity.getUpdatedAt() );

        userProfileResponse.role( entity.getRole().name() );

        return userProfileResponse.build();
    }
}
