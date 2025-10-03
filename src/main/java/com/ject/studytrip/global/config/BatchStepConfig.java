package com.ject.studytrip.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class BatchStepConfig {
    private final Tasklet hardDeleteTasklet;

    private static final String HARD_DELETE_STEP_NAME = "hardDeleteStep";

    @Bean
    public Step hardDeleteStep(
            JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder(HARD_DELETE_STEP_NAME, jobRepository)
                .tasklet(hardDeleteTasklet, transactionManager)
                .build();
    }
}
