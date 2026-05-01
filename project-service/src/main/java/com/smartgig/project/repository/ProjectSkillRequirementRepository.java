package com.smartgig.project.repository;

import com.smartgig.project.entity.ProjectSkillRequirement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectSkillRequirementRepository extends JpaRepository<ProjectSkillRequirement, Long> {
    List<ProjectSkillRequirement> findByProjectId(Long projectId);
}

