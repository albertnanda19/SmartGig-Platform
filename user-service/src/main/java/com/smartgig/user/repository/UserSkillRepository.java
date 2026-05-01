package com.smartgig.user.repository;

import com.smartgig.user.entity.UserSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {
    List<UserSkill> findByUserId(Long userId);

    Optional<UserSkill> findByUserIdAndSkillId(Long userId, Long skillId);

    @Query("SELECT us.skill.id FROM UserSkill us WHERE us.userId = :userId")
    List<Long> findSkillIdsByUserId(@Param("userId") Long userId);

    List<UserSkill> findBySkillId(Long skillId);
}

