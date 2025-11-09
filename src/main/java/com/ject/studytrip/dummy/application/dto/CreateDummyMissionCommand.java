package com.ject.studytrip.dummy.application.dto;

public record CreateDummyMissionCommand(String name) {
    public static CreateDummyMissionCommand of(String name) {
        return new CreateDummyMissionCommand(name);
    }
}
