package com.ject.studytrip.dummy.application.generator;

import com.ject.studytrip.dummy.application.dto.CreateDummyMissionCommand;

public final class CreateDummyMissionCommandGenerator {
    private CreateDummyMissionCommandGenerator() {}

    public static CreateDummyMissionCommand of() {
        return CreateDummyMissionCommand.of("testMission");
    }
}
