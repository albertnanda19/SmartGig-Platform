package com.smartgig.project.repository;

import com.smartgig.project.entity.ProjectApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectApplicationRepository extends JpaRepository<ProjectApplication, Long> {
    boolean existsByProjectIdAndFreelancerId(Long projectId, Long freelancerId);

    List<ProjectApplication> findByProjectId(Long projectId);

    List<ProjectApplication> findByFreelancerId(Long freelancerId);

    Optional<ProjectApplication> findByIdAndProjectId(Long id, Long projectId);
}

