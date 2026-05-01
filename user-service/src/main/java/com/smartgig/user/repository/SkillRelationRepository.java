package com.smartgig.user.repository;

import com.smartgig.user.entity.SkillRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SkillRelationRepository extends JpaRepository<SkillRelation, Long> {
    List<SkillRelation> findBySourceSkillId(Long sourceSkillId);

    List<SkillRelation> findBySourceSkillIdIn(List<Long> sourceSkillIds);

    @Query("SELECT sr FROM SkillRelation sr WHERE sr.sourceSkill.id IN :skillIds OR sr.targetSkill.id IN :skillIds")
    List<SkillRelation> findRelationsBySkillIds(@Param("skillIds") List<Long> skillIds);
}

