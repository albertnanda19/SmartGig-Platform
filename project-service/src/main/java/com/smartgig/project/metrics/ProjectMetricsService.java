package com.smartgig.project.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
@RequiredArgsConstructor
public class ProjectMetricsService {
    private final MeterRegistry meterRegistry;
    private final AtomicLong openProjects = new AtomicLong(0L);

    public void incrementProjectCreated(String category) {
        meterRegistry.counter("smartgig.projects.created", "category", category == null ? "unknown" : category).increment();
    }

    public void incrementApplicationCreated() {
        meterRegistry.counter("smartgig.applications.created").increment();
    }

    public void recordOpenProjectsCount(long count) {
        if (meterRegistry.find("smartgig.projects.open").gauge() == null) {
            Gauge.builder("smartgig.projects.open", openProjects, AtomicLong::get).register(meterRegistry);
        }
        openProjects.set(count);
    }

    public Timer.Sample startMatchingTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopMatchingTimer(Timer.Sample sample) {
        sample.stop(meterRegistry.timer("smartgig.intelligence.matching.duration"));
    }
}

