package com.ject.studytrip.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class BatchJobConfig {
    private final Step hardDeleteStep;

    private static final String HARD_DELETE_JOB_NAME = "hardDeleteJob";

    @Bean
    public Job hardDeleteJob(JobRepository jobRepository) {
        return new JobBuilder(HARD_DELETE_JOB_NAME, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(hardDeleteStep)
                .build();
    }
}
