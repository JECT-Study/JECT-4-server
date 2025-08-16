package com.ject.studytrip.mission.fixture;

import com.ject.studytrip.mission.presentation.dto.request.UpdateMissionRequest;

public class UpdateMissionRequestFixture {
    private String name = null;

    public UpdateMissionRequestFixture withName(String name) {
        this.name = name;
        return this;
    }

    public UpdateMissionRequest build() {
        return new UpdateMissionRequest(name);
    }
}
