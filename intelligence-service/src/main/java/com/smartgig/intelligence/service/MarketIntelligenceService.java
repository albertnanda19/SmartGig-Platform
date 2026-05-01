package com.smartgig.intelligence.service;

import com.smartgig.intelligence.dto.response.MarketPositionResponse;
import com.smartgig.intelligence.dto.response.ProjectRecommendationResponse;

import java.util.List;

public interface MarketIntelligenceService {
    MarketPositionResponse getMarketPosition(Long freelancerId);

    List<ProjectRecommendationResponse> getProjectRecommendations(Long freelancerId, int limit);
}

