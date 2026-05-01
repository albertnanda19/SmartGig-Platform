package com.smartgig.project.repository.search;

import com.smartgig.project.document.ProjectDocument;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ProjectSearchRepository extends ElasticsearchRepository<ProjectDocument, String> {
    List<ProjectDocument> findByStatus(String status);

    List<ProjectDocument> findBySkillNamesContaining(String skillName);

    @Query("{\"bool\": {\"must\": [{\"match\": {\"status\": \"OPEN\"}}], \"filter\": [{\"range\": {\"budgetMin\": {\"gte\": \"?0\"}}}, {\"range\": {\"budgetMax\": {\"lte\": \"?1\"}}}]}}")
    List<ProjectDocument> findByBudgetRange(Double minBudget, Double maxBudget);
}

