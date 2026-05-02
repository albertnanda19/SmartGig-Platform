package com.smartgig.project.health;

import com.smartgig.project.document.ProjectDocument;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Component;

@Component
public class ElasticsearchHealthIndicator implements HealthIndicator {
    private final ElasticsearchOperations elasticsearchOperations;

    public ElasticsearchHealthIndicator(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @Override
    public Health health() {
        try {
            elasticsearchOperations.indexOps(ProjectDocument.class).exists();
            return Health.up().withDetail("elasticsearch", "Available").build();
        } catch (Exception e) {
            return Health.down().withDetail("elasticsearch", "Unavailable: " + e.getMessage()).build();
        }
    }
}

