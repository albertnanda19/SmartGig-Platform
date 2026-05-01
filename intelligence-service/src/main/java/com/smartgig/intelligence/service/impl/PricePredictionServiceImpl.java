package com.smartgig.intelligence.service.impl;

import com.smartgig.intelligence.dto.request.PricePredictionRequest;
import com.smartgig.intelligence.dto.response.PricePredictionResponse;
import com.smartgig.intelligence.engine.PricingEngine;
import com.smartgig.intelligence.engine.PricingEngine.PricePrediction;
import com.smartgig.intelligence.engine.PricingEngine.PricePredictionInput;
import com.smartgig.intelligence.service.PricePredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PricePredictionServiceImpl implements PricePredictionService {
    private final PricingEngine pricingEngine;

    @Override
    @Cacheable(value = "pricePredictions", key = "#request.category + ':' + #request.complexityLevel + ':' + #request.estimatedDurationDays + ':' + #request.requiredSkills.size()")
    public PricePredictionResponse predictPrice(PricePredictionRequest request) {
        PricePrediction prediction = pricingEngine.predict(new PricePredictionInput(
                request.getCategory(),
                request.getComplexityLevel(),
                request.getDeadline(),
                request.getEstimatedDurationDays(),
                request.getRequiredSkills()
        ));

        String fairness = fairnessAssessment(prediction, request.getClientBudgetMin(), request.getClientBudgetMax());
        return PricePredictionResponse.builder()
                .minPrice(prediction.minPrice())
                .maxPrice(prediction.maxPrice())
                .recommendedPrice(prediction.recommendedPrice())
                .confidenceScore(prediction.confidenceScore())
                .priceRange(prediction.priceRange())
                .pricingFactors(prediction.pricingFactors())
                .fairnessAssessment(fairness)
                .marketAveragePrice(null)
                .currency("USD")
                .build();
    }

    private String fairnessAssessment(PricePrediction prediction, Double clientMin, Double clientMax) {
        if (clientMin == null && clientMax == null) {
            return null;
        }
        double rec = prediction.recommendedPrice();
        if (clientMax != null && rec > clientMax) {
            return "CLIENT_BUDGET_BELOW_MARKET";
        }
        if (clientMin != null && rec < clientMin) {
            return "CLIENT_BUDGET_ABOVE_MARKET";
        }
        return "FAIR";
    }
}

