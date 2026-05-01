package com.smartgig.user.service.impl;

import com.smartgig.common.exception.ResourceNotFoundException;
import com.smartgig.user.dto.response.SkillResponse;
import com.smartgig.user.entity.Skill;
import com.smartgig.user.entity.Skill.SkillCategory;
import com.smartgig.user.mapper.SkillMapper;
import com.smartgig.user.repository.SkillRepository;
import com.smartgig.user.service.SkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SkillServiceImpl implements SkillService {
    private final SkillRepository skillRepository;
    private final SkillMapper skillMapper;

    @Override
    @Cacheable(value = "skills", key = "'skills:' + (#category != null ? #category.name() : 'ALL') + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public Page<SkillResponse> getSkills(SkillCategory category, Pageable pageable) {
        Page<Skill> page = category == null ? skillRepository.findAll(pageable) : skillRepository.findByCategory(category, pageable);
        return page.map(skillMapper::toResponse);
    }

    @Override
    @Cacheable(value = "skills", key = "'skill:' + #id")
    public SkillResponse getSkill(Long id) {
        Skill skill = skillRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Skill not found"));
        return skillMapper.toResponse(skill);
    }

    @Override
    @Cacheable(value = "skills", key = "'trending:' + #limit")
    public List<SkillResponse> getTrendingSkills(int limit) {
        return skillRepository.findTopSkills(Pageable.ofSize(limit)).map(skillMapper::toResponse).getContent();
    }
}

