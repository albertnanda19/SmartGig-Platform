package com.smartgig.intelligence.engine;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SkillMatchingEngineTest {

    SkillMatchingEngine engine = new SkillMatchingEngine();

    @Test
    void perfectMatch_allRequiredExpert_scoreOne() {
        Map<String, Double> freelancer = Map.of(
                "Java", 1.0,
                "Spring Boot", 1.0,
                "Kafka", 1.0
        );
        var score = engine.calculate(freelancer, List.of("Java", "Spring Boot", "Kafka"), List.of());
        assertThat(score.overallScore()).isEqualTo(1.0);
    }

    @Test
    void noMatch_requiredMissing_scoreZero() {
        Map<String, Double> freelancer = Map.of("React", 1.0);
        var score = engine.calculate(freelancer, List.of("Java", "Spring Boot"), List.of());
        assertThat(score.overallScore()).isEqualTo(0.0);
    }

    @Test
    void partialMatch_scoreBetween() {
        Map<String, Double> freelancer = Map.of("Java", 0.5);
        var score = engine.calculate(freelancer, List.of("Java", "Spring Boot"), List.of());
        assertThat(score.overallScore()).isGreaterThan(0.0);
        assertThat(score.overallScore()).isLessThan(0.7);
    }

    @Test
    void bonusExpert_moreThanThreeAdvancedOrExpert_addsBonus() {
        Map<String, Double> freelancer = Map.of(
                "Java", 1.0,
                "Spring Boot", 1.0,
                "Kafka", 0.75,
                "Docker", 0.75
        );
        var score = engine.calculate(freelancer, List.of("Java", "Spring Boot", "Kafka", "Docker"), List.of());
        assertThat(score.overallScore()).isGreaterThan(0.9);
    }
}

