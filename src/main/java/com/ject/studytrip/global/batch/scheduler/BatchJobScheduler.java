package com.ject.studytrip.global.batch.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Profile("!test")
@Component
@RequiredArgsConstructor
public class BatchJobScheduler {
    private final JobLauncher jobLauncher;
    private final Job hardDeleteJob;

    // 매일 04:00 (Asia/Seoul)
    @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
    public void runHardDeleteJob() throws Exception {
        JobParameters jobParameters =
                new JobParametersBuilder()
                        .addLong("ts", System.currentTimeMillis())
                        .toJobParameters();

        jobLauncher.run(hardDeleteJob, jobParameters);
    }
}
