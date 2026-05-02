package com.smartgig.analytics.job;

import com.smartgig.analytics.service.TrendAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProjectAnalyticsJob implements Job {
    private final TrendAnalysisService trendAnalysisService;

    @Override
    public void execute(JobExecutionContext context) {
        trendAnalysisService.calculateProjectAnalytics();
    }
}

