package com.ject.studytrip.global.config;

import com.ject.studytrip.cleanup.application.facade.HardDeleteFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class BatchJobConfig {
    private final JobRepository jobRepository;
    private final HardDeleteFacade hardDeleteFacade;
    private final PlatformTransactionManager transactionManager;

    private static final String HARD_DELETE_JOB_NAME = "hardDeleteJob";
    private static final String HARD_DELETE_STEP_NAME = "hardDeleteStep";

    @Bean
    public Job hardDeleteJob(Step hardDeleteStep) {
        return new JobBuilder(HARD_DELETE_JOB_NAME, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(hardDeleteStep)
                .build();
    }

    @Bean
    public Step hardDeleteStep(Tasklet hardDeleteTasklet) {
        return new StepBuilder(HARD_DELETE_STEP_NAME, jobRepository)
                .tasklet(hardDeleteTasklet, transactionManager)
                .build();
    }

    @Bean
    public Tasklet hardDeleteTasklet() {
        return (contribution, chunkContext) -> {
            hardDeleteFacade.hardDeleteAll();
            return RepeatStatus.FINISHED;
        };
    }
}
