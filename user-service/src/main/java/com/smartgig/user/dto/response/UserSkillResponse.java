package com.smartgig.user.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserSkillResponse {
    private Long userId;
    private SkillResponse skill;
    private String proficiencyLevel;
    private Integer yearsOfExperience;
    private Boolean primary;
}

