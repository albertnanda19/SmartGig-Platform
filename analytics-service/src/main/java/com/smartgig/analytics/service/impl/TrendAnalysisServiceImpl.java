package com.smartgig.analytics.service.impl;

import com.smartgig.analytics.document.EventLog;
import com.smartgig.analytics.document.ProjectAnalytics;
import com.smartgig.analytics.document.SkillTrend;
import com.smartgig.analytics.repository.ProjectAnalyticsRepository;
import com.smartgig.analytics.repository.SkillTrendRepository;
import com.smartgig.analytics.service.TrendAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.WeekFields;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrendAnalysisServiceImpl implements TrendAnalysisService {
    private final MongoTemplate mongoTemplate;
    private final SkillTrendRepository skillTrendRepository;
    private final ProjectAnalyticsRepository projectAnalyticsRepository;

    @Override
    public void calculateWeeklySkillTrends() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        String weekLabel = weekLabel(now);
        LocalDateTime since = now.minusDays(7);

        List<EventLog> created = mongoTemplate.find(
                Query.query(Criteria.where("eventType").is("project.created").and("receivedAt").gte(since)),
                EventLog.class
        );

        Map<String, Integer> demand = new HashMap<>();
        Map<String, Double> budgetSum = new HashMap<>();
        Map<String, Integer> budgetCount = new HashMap<>();

        for (EventLog log : created) {
            Object skills = log.getPayload().get("requiredSkills");
            Object min = log.getPayload().get("budgetMin");
            Object max = log.getPayload().get("budgetMax");
            double mid = midBudget(min, max);
            if (skills instanceof List<?> list) {
                for (Object s : list) {
                    if (s == null) continue;
                    String name = s.toString();
                    demand.put(name, demand.getOrDefault(name, 0) + 1);
                    budgetSum.put(name, budgetSum.getOrDefault(name, 0.0) + mid);
                    budgetCount.put(name, budgetCount.getOrDefault(name, 0) + 1);
                }
            }
        }

        List<Map.Entry<String, Integer>> ranked = demand.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()))
                .toList();

        int rank = 1;
        for (Map.Entry<String, Integer> entry : ranked) {
            String skillName = entry.getKey();
            int current = entry.getValue();
            double avgBudget = budgetCount.getOrDefault(skillName, 0) == 0 ? 0.0 : budgetSum.getOrDefault(skillName, 0.0) / budgetCount.get(skillName);
            double growthRate = growthRate(skillName, weekLabel, current);

            SkillTrend trend = skillTrendRepository.findBySkillNameAndWeekLabel(skillName, weekLabel)
                    .orElse(SkillTrend.builder().skillName(skillName).weekLabel(weekLabel).build());
            trend.setDemandCount(current);
            trend.setAvgProjectBudget(avgBudget);
            trend.setGrowthRate(growthRate);
            trend.setRank(rank++);
            skillTrendRepository.save(trend);
        }
    }

    @Override
    public void calculateProjectAnalytics() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime since = now.minusDays(7);
        String periodLabel = weekLabel(now);

        List<EventLog> created = mongoTemplate.find(
                Query.query(Criteria.where("eventType").is("project.created").and("receivedAt").gte(since)),
                EventLog.class
        );
        List<EventLog> applied = mongoTemplate.find(
                Query.query(Criteria.where("eventType").is("project.applied").and("receivedAt").gte(since)),
                EventLog.class
        );
        List<EventLog> status = mongoTemplate.find(
                Query.query(Criteria.where("eventType").is("project.status.changed").and("receivedAt").gte(since)),
                EventLog.class
        );

        int totalCreated = created.size();
        int totalApplications = applied.size();
        int completed = (int) status.stream().filter(e -> "COMPLETED".equals(value(e.getPayload(), "newStatus"))).count();
        int cancelled = (int) status.stream().filter(e -> "CANCELLED".equals(value(e.getPayload(), "newStatus"))).count();

        double avgBudget = created.isEmpty() ? 0.0 : created.stream().mapToDouble(e -> midBudget(e.getPayload().get("budgetMin"), e.getPayload().get("budgetMax"))).average().orElse(0.0);

        Map<String, Integer> byCategory = new HashMap<>();
        Map<String, Double> avgBudgetByCategory = new HashMap<>();
        Map<String, Double> catSum = new HashMap<>();
        Map<String, Integer> catCount = new HashMap<>();

        for (EventLog e : created) {
            String cat = value(e.getPayload(), "category");
            double mid = midBudget(e.getPayload().get("budgetMin"), e.getPayload().get("budgetMax"));
            if (cat != null) {
                byCategory.put(cat, byCategory.getOrDefault(cat, 0) + 1);
                catSum.put(cat, catSum.getOrDefault(cat, 0.0) + mid);
                catCount.put(cat, catCount.getOrDefault(cat, 0) + 1);
            }
        }

        for (String cat : catSum.keySet()) {
            avgBudgetByCategory.put(cat, catSum.get(cat) / Math.max(catCount.getOrDefault(cat, 1), 1));
        }

        double avgApplicationsPerProject = totalCreated == 0 ? 0.0 : (double) totalApplications / (double) totalCreated;

        projectAnalyticsRepository.save(ProjectAnalytics.builder()
                .periodLabel(periodLabel)
                .periodType("WEEKLY")
                .totalProjectsCreated(totalCreated)
                .totalProjectsCompleted(completed)
                .totalProjectsCancelled(cancelled)
                .avgBudget(avgBudget)
                .totalValueCompleted(0.0)
                .projectsByCategory(byCategory)
                .avgBudgetByCategory(avgBudgetByCategory)
                .totalApplications(totalApplications)
                .avgApplicationsPerProject(avgApplicationsPerProject)
                .build());
    }

    private String weekLabel(LocalDateTime dt) {
        WeekFields wf = WeekFields.of(Locale.US);
        int week = dt.get(wf.weekOfWeekBasedYear());
        int year = dt.get(wf.weekBasedYear());
        return year + "-W" + String.format("%02d", week);
    }

    private double growthRate(String skillName, String currentWeek, int currentCount) {
        Query q = Query.query(
                Criteria.where("skillName").is(skillName).and("weekLabel").ne(currentWeek)
        );
        q.limit(1);
        q.with(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "weekLabel"));
        SkillTrend prev = mongoTemplate.findOne(q, SkillTrend.class);
        if (prev == null) {
            return 0.0;
        }
        int prevCount = prev.getDemandCount();
        if (prevCount <= 0) {
            return currentCount > 0 ? 1.0 : 0.0;
        }
        return ((double) currentCount - (double) prevCount) / (double) prevCount;
    }

    private double midBudget(Object min, Object max) {
        double a = num(min);
        double b = num(max);
        if (a == 0.0 && b == 0.0) return 0.0;
        return (a + b) / 2.0;
    }

    private double num(Object v) {
        if (v == null) return 0.0;
        if (v instanceof Number n) return n.doubleValue();
        try {
            return Double.parseDouble(v.toString());
        } catch (Exception e) {
            return 0.0;
        }
    }

    private String value(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v == null ? null : v.toString();
    }
}

