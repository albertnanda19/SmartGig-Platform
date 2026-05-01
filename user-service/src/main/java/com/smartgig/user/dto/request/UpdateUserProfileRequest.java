package com.smartgig.user.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateUserProfileRequest {
    private String fullName;
    private String bio;
    private String avatarUrl;
    private BigDecimal hourlyRate;
    private Integer yearsOfExperience;
    private String location;
    private String portfolioUrl;
    private String linkedinUrl;
    private String githubUrl;
    private Boolean available;
}

