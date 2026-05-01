package com.smartgig.intelligence.service;

import com.smartgig.intelligence.dto.response.MatchingScoreResponse;

import java.util.List;

public interface MatchingService {
    MatchingScoreResponse calculateMatchingScore(Long freelancerId, Long projectId);

    List<MatchingScoreResponse> getBestFreelancersForProject(Long projectId, int limit);
}

