package com.ject.studytrip.mission.fixture;

import com.ject.studytrip.mission.presentation.dto.request.CreateMissionRequest;

public class CreateMissionRequestFixture {
    private String name = "TEST MISSION NAME";

    public CreateMissionRequestFixture withName(String name) {
        this.name = name;
        return this;
    }

    public CreateMissionRequest build() {
        return new CreateMissionRequest(name);
    }
}
