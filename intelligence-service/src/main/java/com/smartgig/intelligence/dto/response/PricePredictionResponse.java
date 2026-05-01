package com.smartgig.intelligence.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PricePredictionResponse {
    private double minPrice;
    private double maxPrice;
    private double recommendedPrice;
    private double confidenceScore;
    private String priceRange;
    private List<String> pricingFactors;
    private String fairnessAssessment;
    private Double marketAveragePrice;
    private String currency;
}

