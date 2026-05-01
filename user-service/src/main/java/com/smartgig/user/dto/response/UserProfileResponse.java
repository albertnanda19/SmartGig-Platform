package com.smartgig.user.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class UserProfileResponse {
    private Long userId;
    private String username;
    private String email;
    private String fullName;
    private String role;
    private String bio;
    private String avatarUrl;
    private BigDecimal hourlyRate;
    private Integer yearsOfExperience;
    private String location;
    private String portfolioUrl;
    private String linkedinUrl;
    private String githubUrl;
    private Integer totalProjectsCompleted;
    private BigDecimal totalEarnings;
    private BigDecimal averageRating;
    private Boolean available;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

