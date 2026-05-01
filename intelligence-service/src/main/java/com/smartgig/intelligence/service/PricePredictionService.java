package com.smartgig.intelligence.service;

import com.smartgig.intelligence.dto.request.PricePredictionRequest;
import com.smartgig.intelligence.dto.response.PricePredictionResponse;

public interface PricePredictionService {
    PricePredictionResponse predictPrice(PricePredictionRequest request);
}

