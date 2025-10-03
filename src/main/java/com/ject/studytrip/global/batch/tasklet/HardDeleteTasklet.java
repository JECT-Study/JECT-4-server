package com.ject.studytrip.global.batch.tasklet;

import com.ject.studytrip.cleanup.application.facade.HardDeleteFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HardDeleteTasklet implements Tasklet {
    private final HardDeleteFacade hardDeleteFacade;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
            throws Exception {
        hardDeleteFacade.hardDeleteAll();

        return RepeatStatus.FINISHED;
    }
}
