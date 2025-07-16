package com.ject.studytrip.mission.fixture;

import com.ject.studytrip.mission.presentation.dto.request.UpdateMissionRequest;

public class UpdateMissionRequestFixture {
    private String name = null;
    private String memo = null;

    public UpdateMissionRequestFixture withName(String name) {
        this.name = name;
        return this;
    }

    public UpdateMissionRequestFixture withMemo(String memo) {
        this.memo = memo;
        return this;
    }

    public UpdateMissionRequest build() {
        return new UpdateMissionRequest(name, memo);
    }
}
