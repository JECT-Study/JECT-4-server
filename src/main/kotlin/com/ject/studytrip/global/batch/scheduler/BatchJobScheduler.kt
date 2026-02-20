package com.ject.studytrip.global.batch.scheduler

import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Profile("!test")
@Component
class BatchJobScheduler(
    private val jobLauncher: JobLauncher,
    private val hardDeleteJob: Job,
) {
    companion object {
        private const val JOB_PARAMETER_TIMESTAMP = "ts"
    }

    /**
     * 매일 04:00 (Asia/Seoul)
     */
    @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
    fun runHardDeleteJob() {
        jobLauncher.run(hardDeleteJob, createJobParameters())
    }

    private fun createJobParameters(): JobParameters =
        JobParametersBuilder()
            .addLong(JOB_PARAMETER_TIMESTAMP, System.currentTimeMillis())
            .toJobParameters()
}
