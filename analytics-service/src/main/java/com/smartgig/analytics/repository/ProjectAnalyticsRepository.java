package com.smartgig.analytics.repository;

import com.smartgig.analytics.document.ProjectAnalytics;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ProjectAnalyticsRepository extends MongoRepository<ProjectAnalytics, String> {
    Optional<ProjectAnalytics> findFirstByOrderByCalculatedAtDesc();
}

