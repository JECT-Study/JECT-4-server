package com.ject.studytrip.global.batch.tasklet

import com.ject.studytrip.cleanup.application.facade.HardDeleteFacade
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.stereotype.Component

@Component
class HardDeleteTasklet(
    private val hardDeleteFacade: HardDeleteFacade,
) : Tasklet {
    override fun execute(
        contribution: StepContribution,
        chunkContext: ChunkContext,
    ): RepeatStatus {
        hardDeleteFacade.hardDeleteAll()

        return RepeatStatus.FINISHED
    }
}
