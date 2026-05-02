package com.smartgig.analytics.config;

import com.smartgig.analytics.job.ProjectAnalyticsJob;
import com.smartgig.analytics.job.SkillTrendJob;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.CronScheduleBuilder.cronSchedule;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail skillTrendJobDetail() {
        return newJob(SkillTrendJob.class)
                .withIdentity("skillTrendJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger skillTrendJobTrigger(JobDetail skillTrendJobDetail) {
        return newTrigger()
                .forJob(skillTrendJobDetail)
                .withIdentity("skillTrendTrigger")
                .withSchedule(cronSchedule("0 0 2 * * ?"))
                .build();
    }

    @Bean
    public JobDetail projectAnalyticsJobDetail() {
        return newJob(ProjectAnalyticsJob.class)
                .withIdentity("projectAnalyticsJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger projectAnalyticsJobTrigger(JobDetail projectAnalyticsJobDetail) {
        return newTrigger()
                .forJob(projectAnalyticsJobDetail)
                .withIdentity("projectAnalyticsTrigger")
                .withSchedule(cronSchedule("0 30 2 * * ?"))
                .build();
    }
}

