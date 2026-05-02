package com.smartgig.analytics.repository;

import com.smartgig.analytics.document.SkillTrend;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SkillTrendRepository extends MongoRepository<SkillTrend, String> {
    List<SkillTrend> findByWeekLabelOrderByRankAsc(String weekLabel);

    List<SkillTrend> findBySkillIdOrderByWeekLabelDesc(Long skillId);

    Optional<SkillTrend> findBySkillNameAndWeekLabel(String skillName, String weekLabel);
}

