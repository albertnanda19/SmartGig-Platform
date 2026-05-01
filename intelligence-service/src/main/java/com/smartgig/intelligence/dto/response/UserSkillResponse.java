package com.smartgig.intelligence.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserSkillResponse {
    private Long userId;
    private SkillResponse skill;
    private String proficiencyLevel;
}

