package com.ject.studytrip.mission.fixture;

import com.ject.studytrip.mission.presentation.dto.request.UpdateMissionOrderRequest;
import java.util.List;

public class UpdateMissionOrderRequestFixture {
    private List<Long> orderedIds;

    public UpdateMissionOrderRequestFixture withOrderedIds(List<Long> orderedIds) {
        this.orderedIds = orderedIds;
        return this;
    }

    public UpdateMissionOrderRequest build() {
        return new UpdateMissionOrderRequest(orderedIds);
    }
}
