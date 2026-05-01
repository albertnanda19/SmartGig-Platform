package com.smartgig.intelligence.engine;

import com.smartgig.intelligence.util.ScoringUtil;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

@Component
public class RecommendationEngine {

    public double scoreProjectForFreelancer(
            ProjectScoringContext projectCtx,
            FreelancerScoringContext freelancerCtx,
            SkillMatchingEngine.MatchingScore skillScore
    ) {
        double skill = ScoringUtil.clamp01(skillScore.overallScore());

        double budgetMid = (projectCtx.budgetMin() + projectCtx.budgetMax()) / 2.0;
        double expectedDaily = freelancerCtx.hourlyRate() * 8.0;
        double budgetFit = ScoringUtil.clamp01(1.0 - Math.abs(budgetMid - expectedDaily) / Math.max(expectedDaily, 1.0));

        long ageHours = Math.max(0, ChronoUnit.HOURS.between(projectCtx.createdAt(), LocalDateTime.now(ZoneOffset.UTC)));
        double recency = ScoringUtil.clamp01(1.0 - (ageHours / (24.0 * 7.0)));

        double popularity = ScoringUtil.normalize(projectCtx.viewsCount(), 0.0, 500.0);

        double availability = ScoringUtil.clamp01(1.0 - ((double) projectCtx.currentApplicantCount() / Math.max(projectCtx.maxApplicants(), 1)));

        double composite = (skill * 0.40) + (budgetFit * 0.20) + (recency * 0.20) + (popularity * 0.10) + (availability * 0.10);
        return ScoringUtil.clamp01(composite);
    }

    public record ProjectScoringContext(
            Long projectId,
            double budgetMin,
            double budgetMax,
            LocalDateTime createdAt,
            int viewsCount,
            int currentApplicantCount,
            int maxApplicants
    ) {
    }

    public record FreelancerScoringContext(
            Long userId,
            double hourlyRate,
            int yearsExperience
    ) {
    }
}

