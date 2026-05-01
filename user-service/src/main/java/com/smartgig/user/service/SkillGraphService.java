package com.smartgig.user.service;

import com.smartgig.user.dto.response.SkillGapAnalysisResponse;
import com.smartgig.user.dto.response.SkillResponse;
import com.smartgig.user.dto.response.SkillSimilarityResponse;

import java.util.List;

public interface SkillGraphService {
    SkillSimilarityResponse calculateSkillSimilarity(Long userId1, Long userId2);

    SkillGapAnalysisResponse getSkillGapAnalysis(Long userId);

    List<SkillResponse> getRelatedSkills(Long skillId, int depth);
}

