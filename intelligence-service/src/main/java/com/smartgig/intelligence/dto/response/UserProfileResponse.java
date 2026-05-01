package com.smartgig.intelligence.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class UserProfileResponse {
    private Long userId;
    private String username;
    private String email;
    private String fullName;
    private String role;
    private BigDecimal hourlyRate;
    private Integer yearsOfExperience;
    private BigDecimal averageRating;
    private Integer totalProjectsCompleted;
}

