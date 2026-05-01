package com.smartgig.user.repository;

import com.smartgig.user.entity.Skill;
import com.smartgig.user.entity.Skill.SkillCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    Optional<Skill> findBySlug(String slug);

    List<Skill> findByCategory(SkillCategory category);

    Page<Skill> findByCategory(SkillCategory category, Pageable pageable);

    @Query("SELECT s FROM Skill s ORDER BY s.usageCount DESC")
    Page<Skill> findTopSkills(Pageable pageable);
}

