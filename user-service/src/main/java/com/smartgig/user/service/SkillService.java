package com.smartgig.user.service;

import com.smartgig.user.dto.response.SkillResponse;
import com.smartgig.user.entity.Skill.SkillCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SkillService {
    Page<SkillResponse> getSkills(SkillCategory category, Pageable pageable);

    SkillResponse getSkill(Long id);

    List<SkillResponse> getTrendingSkills(int limit);
}

