package com.ject.studytrip.global.config

import com.ject.studytrip.global.common.constants.BatchConstants.HARD_DELETE_JOB
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BatchJobConfig(
    private val hardDeleteStep: Step,
) {
    @Bean
    fun hardDeleteJob(jobRepository: JobRepository): Job =
        JobBuilder(HARD_DELETE_JOB, jobRepository)
            .incrementer(RunIdIncrementer())
            .start(hardDeleteStep)
            .build()
}
