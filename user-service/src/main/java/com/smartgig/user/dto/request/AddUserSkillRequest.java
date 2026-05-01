package com.smartgig.user.dto.request;

import com.smartgig.user.entity.UserSkill;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddUserSkillRequest {
    @NotNull
    private Long skillId;

    private UserSkill.ProficiencyLevel proficiencyLevel;
    private Integer yearsOfExperience;
    private Boolean primary;
}

