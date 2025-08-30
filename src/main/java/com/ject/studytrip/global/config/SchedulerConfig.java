package com.ject.studytrip.global.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Profile("!test")
@EnableScheduling
@Configuration
public class SchedulerConfig {
    private final JobLauncher jobLauncher;
    private final Job hardDeleteJob;

    public SchedulerConfig(JobLauncher jobLauncher, @Qualifier("hardDeleteJob") Job hardDeleteJob) {
        this.jobLauncher = jobLauncher;
        this.hardDeleteJob = hardDeleteJob;
    }

    // 매일 04:00 (Asia/Seoul)
    @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
    public void runDaily() throws Exception {
        jobLauncher.run(
                hardDeleteJob,
                new JobParametersBuilder()
                        .addLong("ts", System.currentTimeMillis()) // 매 실행마다 유니크 파라미터
                        .toJobParameters());
    }
}
