package com.niu.community.quartz.config;

import com.niu.community.quartz.job.HotRankingJob;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

@Configuration
public class QuartzJobConfig {

    @Bean
    public JobDetail hotRankingDetail() {
        return newJob(HotRankingJob.class)
            .withIdentity("hotRankingJob")
            .storeDurably()
            .build();
    }

    @Bean
    public Trigger hotRankingTrigger(JobDetail hotRankingDetail) {
        return newTrigger()
            .forJob(hotRankingDetail)
            .withIdentity("hotRankingTrigger")
            .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInMinutes(30).repeatForever())
            .build();
    }
}
