package com.smartgig.project.repository;

import com.smartgig.project.entity.Project;
import com.smartgig.project.statemachine.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    boolean existsBySlug(String slug);

    Optional<Project> findByIdAndClientId(Long id, Long clientId);

    Page<Project> findByStatus(ProjectStatus status, Pageable pageable);
}

