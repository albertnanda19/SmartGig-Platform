package com.smartgig.intelligence.engine;

import com.smartgig.intelligence.util.ScoringUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class SkillMatchingEngine {

    public MatchingScore calculate(
            Map<String, Double> freelancerSkills,
            List<String> requiredSkills,
            List<String> optionalSkills
    ) {
        List<String> matchedRequired = new ArrayList<>();
        List<String> missingRequired = new ArrayList<>();
        List<String> matchedOptional = new ArrayList<>();

        double requiredScore = scoreSkills(freelancerSkills, requiredSkills, matchedRequired, missingRequired);
        boolean hasOptional = optionalSkills != null && !optionalSkills.isEmpty();
        double optionalScore = hasOptional ? scoreSkills(freelancerSkills, optionalSkills, matchedOptional, null) : 0.0;

        double overall = hasOptional ? ((requiredScore * 0.7) + (optionalScore * 0.3)) : requiredScore;
        overall = ScoringUtil.clamp01(overall + bonusAdvancedRequired(freelancerSkills, requiredSkills));

        return new MatchingScore(
                overall,
                requiredScore,
                optionalScore,
                matchedRequired,
                missingRequired,
                matchedOptional,
                levelOf(overall)
        );
    }

    private double scoreSkills(
            Map<String, Double> freelancerSkills,
            List<String> skills,
            List<String> matched,
            List<String> missing
    ) {
        if (skills == null || skills.isEmpty()) {
            return 0.0;
        }
        double sum = 0.0;
        for (String required : skills) {
            double best = 0.0;
            String bestKey = null;
            for (Map.Entry<String, Double> entry : freelancerSkills.entrySet()) {
                String key = entry.getKey();
                double prof = entry.getValue() == null ? 0.0 : entry.getValue();
                double s = matchScore(required, key) * prof;
                if (s > best) {
                    best = s;
                    bestKey = required;
                }
            }
            sum += best;
            if (best > 0.0) {
                if (matched != null) {
                    matched.add(bestKey);
                }
            } else {
                if (missing != null) {
                    missing.add(required);
                }
            }
        }
        return ScoringUtil.clamp01(sum / (double) skills.size());
    }

    private double matchScore(String required, String candidate) {
        if (required == null || candidate == null) {
            return 0.0;
        }
        String r = required.toLowerCase(Locale.ROOT).trim();
        String c = candidate.toLowerCase(Locale.ROOT).trim();
        if (r.equals(c)) {
            return 1.0;
        }
        if (r.contains(c) || c.contains(r)) {
            return 0.6;
        }
        return 0.0;
    }

    private double bonusAdvancedRequired(Map<String, Double> freelancerSkills, List<String> requiredSkills) {
        if (requiredSkills == null || requiredSkills.isEmpty()) {
            return 0.0;
        }
        int count = 0;
        for (String req : requiredSkills) {
            Double prof = bestProficiencyFor(req, freelancerSkills);
            if (prof != null && prof >= 0.75) {
                count++;
            }
        }
        return count > 3 ? 0.05 : 0.0;
    }

    private Double bestProficiencyFor(String required, Map<String, Double> freelancerSkills) {
        Double best = null;
        for (Map.Entry<String, Double> e : freelancerSkills.entrySet()) {
            if (matchScore(required, e.getKey()) > 0.0) {
                double v = e.getValue() == null ? 0.0 : e.getValue();
                if (best == null || v > best) {
                    best = v;
                }
            }
        }
        return best;
    }

    private String levelOf(double score) {
        if (score > 0.9) return "PERFECT";
        if (score >= 0.7) return "STRONG";
        if (score >= 0.5) return "GOOD";
        if (score >= 0.3) return "PARTIAL";
        return "WEAK";
    }

    public record MatchingScore(
            double overallScore,
            double requiredSkillsScore,
            double optionalSkillsScore,
            List<String> matchedRequiredSkills,
            List<String> missingRequiredSkills,
            List<String> matchedOptionalSkills,
            String matchLevel
    ) {
    }
}

