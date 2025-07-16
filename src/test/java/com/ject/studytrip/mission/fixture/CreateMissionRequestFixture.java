package com.ject.studytrip.mission.fixture;

import com.ject.studytrip.mission.presentation.dto.request.CreateMissionRequest;

public class CreateMissionRequestFixture {
    private String name = "TEST MISSION NAME";
    private String memo = "TEST MISSION MEMO";
    private int missionOrder = 1;

    public CreateMissionRequestFixture withName(String name) {
        this.name = name;
        return this;
    }

    public CreateMissionRequestFixture withMemo(String memo) {
        this.memo = memo;
        return this;
    }

    public CreateMissionRequestFixture withMissionOrder(int missionOrder) {
        this.missionOrder = missionOrder;
        return this;
    }

    public CreateMissionRequest build() {
        return new CreateMissionRequest(name, memo, missionOrder);
    }
}
