package com.smartgig.intelligence.engine;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PricingEngineTest {

    PricingEngine engine = new PricingEngine();

    @Test
    void urgentDeadline_increasesPrice() {
        var urgent = engine.predict(new PricingEngine.PricePredictionInput(
                "BACKEND",
                "MEDIUM",
                LocalDateTime.now().plusDays(3),
                10,
                List.of("Java", "Spring Boot")
        ));
        var relaxed = engine.predict(new PricingEngine.PricePredictionInput(
                "BACKEND",
                "MEDIUM",
                LocalDateTime.now().plusDays(40),
                10,
                List.of("Java", "Spring Boot")
        ));
        assertThat(urgent.recommendedPrice()).isGreaterThan(relaxed.recommendedPrice());
    }

    @Test
    void complexityMultiplier_complexGreaterThanMediumGreaterThanSimple() {
        var simple = engine.predict(new PricingEngine.PricePredictionInput("BACKEND", "SIMPLE", null, 10, List.of("Java")));
        var medium = engine.predict(new PricingEngine.PricePredictionInput("BACKEND", "MEDIUM", null, 10, List.of("Java")));
        var complex = engine.predict(new PricingEngine.PricePredictionInput("BACKEND", "COMPLEX", null, 10, List.of("Java")));
        assertThat(complex.recommendedPrice()).isGreaterThan(medium.recommendedPrice());
        assertThat(medium.recommendedPrice()).isGreaterThan(simple.recommendedPrice());
    }

    @Test
    void categoryAffectsBaseRate_aiMlHigherThanDesign() {
        var ai = engine.predict(new PricingEngine.PricePredictionInput("AI_ML", "MEDIUM", null, 10, List.of("Machine Learning")));
        var design = engine.predict(new PricingEngine.PricePredictionInput("DESIGN", "MEDIUM", null, 10, List.of("UI")));
        assertThat(ai.recommendedPrice()).isGreaterThan(design.recommendedPrice());
    }

    @Test
    void recommendedBetweenMinAndMax() {
        var pred = engine.predict(new PricingEngine.PricePredictionInput("BACKEND", "MEDIUM", null, 10, List.of("Java")));
        assertThat(pred.recommendedPrice()).isGreaterThanOrEqualTo(pred.minPrice());
        assertThat(pred.recommendedPrice()).isLessThanOrEqualTo(pred.maxPrice());
    }
}

