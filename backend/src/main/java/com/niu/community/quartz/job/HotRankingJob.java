package com.niu.community.quartz.job;

import com.niu.community.ranking.service.RankingService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Component
public class HotRankingJob implements Job {

    private final RankingService rankingService;

    public HotRankingJob(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        rankingService.refreshHotRanking();
    }
}
