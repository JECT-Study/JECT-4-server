package com.ject.studytrip.global.config

import com.ject.studytrip.global.batch.tasklet.HardDeleteTasklet
import com.ject.studytrip.global.common.constants.BatchConstants.HARD_DELETE_STEP
import org.springframework.batch.core.Step
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class BatchStepConfig(
    private val hardDeleteTasklet: HardDeleteTasklet,
) {
    @Bean
    fun hardDeleteStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
    ): Step =
        StepBuilder(HARD_DELETE_STEP, jobRepository)
            .tasklet(hardDeleteTasklet, transactionManager)
            .build()
}
