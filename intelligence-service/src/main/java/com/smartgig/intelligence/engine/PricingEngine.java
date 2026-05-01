package com.smartgig.intelligence.engine;

import com.smartgig.intelligence.util.ScoringUtil;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class PricingEngine {

    private static final Map<String, Double[]> BASE_RATES = Map.of(
            "BACKEND", new Double[]{50.0, 150.0},
            "FRONTEND", new Double[]{40.0, 120.0},
            "MOBILE", new Double[]{60.0, 180.0},
            "DEVOPS", new Double[]{70.0, 200.0},
            "AI_ML", new Double[]{80.0, 250.0},
            "FULLSTACK", new Double[]{60.0, 180.0},
            "DATA", new Double[]{65.0, 190.0},
            "DESIGN", new Double[]{30.0, 100.0}
    );

    private static final Map<String, Double> COMPLEXITY_MULTIPLIER = Map.of(
            "SIMPLE", 0.7,
            "LOW", 0.85,
            "MEDIUM", 1.0,
            "HIGH", 1.3,
            "COMPLEX", 1.6
    );

    public PricePrediction predict(PricePredictionInput input) {
        List<String> factors = new ArrayList<>();

        String category = normalize(input.category());
        Double[] base = BASE_RATES.getOrDefault(category, new Double[]{45.0, 140.0});
        double baseMin = base[0];
        double baseMax = base[1];
        double baseMid = (baseMin + baseMax) / 2.0;
        factors.add("Base rate category: " + category);

        String complexity = normalize(input.complexityLevel());
        double complexityMul = COMPLEXITY_MULTIPLIER.getOrDefault(complexity, 1.0);
        baseMin *= complexityMul;
        baseMax *= complexityMul;
        baseMid *= complexityMul;
        factors.add("Complexity multiplier: " + complexityMul);

        double urgencyMul = urgencyMultiplier(input.deadline());
        baseMin *= urgencyMul;
        baseMax *= urgencyMul;
        baseMid *= urgencyMul;
        factors.add("Deadline urgency multiplier: " + urgencyMul);

        int skillCount = input.requiredSkills() == null ? 0 : input.requiredSkills().size();
        double skillsMul = skillsMultiplier(skillCount);
        baseMin *= skillsMul;
        baseMax *= skillsMul;
        baseMid *= skillsMul;
        factors.add("Required skills multiplier: " + skillsMul);

        int days = Math.max(input.estimatedDurationDays(), 1);
        double durationPrice = days * (baseMid / 8.0);
        factors.add("Duration days: " + days);

        double min = Math.min(baseMin, durationPrice);
        double max = Math.max(baseMax, durationPrice);
        double recommended = (min + max) / 2.0;

        double confidence = confidenceScore(category, complexity, input.deadline(), days, skillCount);
        String priceRange = recommended > base[1] * 1.1 ? "PREMIUM" : (recommended < base[0] * 0.9 ? "LOW" : "MARKET");

        return new PricePrediction(
                round2(min),
                round2(max),
                round2(recommended),
                ScoringUtil.clamp01(confidence),
                priceRange,
                factors
        );
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private double urgencyMultiplier(LocalDateTime deadline) {
        if (deadline == null) {
            return 1.0;
        }
        long days = ChronoUnit.DAYS.between(LocalDateTime.now(), deadline);
        if (days < 7) return 1.30;
        if (days <= 14) return 1.15;
        if (days <= 30) return 1.05;
        return 1.0;
    }

    private double skillsMultiplier(int skillCount) {
        if (skillCount <= 2) return 1.0;
        if (skillCount <= 5) return 1.10;
        return 1.20;
    }

    private double confidenceScore(String category, String complexity, LocalDateTime deadline, int days, int skillCount) {
        double score = 0.4;
        if (BASE_RATES.containsKey(category)) score += 0.2;
        if (COMPLEXITY_MULTIPLIER.containsKey(complexity)) score += 0.1;
        if (deadline != null) score += 0.1;
        if (days > 0) score += 0.1;
        if (skillCount > 0) score += 0.1;
        return score;
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    public record PricePredictionInput(
            String category,
            String complexityLevel,
            LocalDateTime deadline,
            int estimatedDurationDays,
            List<String> requiredSkills
    ) {
    }

    public record PricePrediction(
            double minPrice,
            double maxPrice,
            double recommendedPrice,
            double confidenceScore,
            String priceRange,
            List<String> pricingFactors
    ) {
    }
}

