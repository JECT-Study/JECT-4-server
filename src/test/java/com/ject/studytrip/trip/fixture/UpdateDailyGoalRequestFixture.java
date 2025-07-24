package com.ject.studytrip.trip.fixture;

import com.ject.studytrip.trip.presentation.dto.request.UpdateDailyGoalRequest;
import java.util.List;

public class UpdateDailyGoalRequestFixture {
    private List<Long> deleteDailyMissionIds = null;
    private List<Long> addMissionIds = null;

    public UpdateDailyGoalRequestFixture withDeleteDailyMissionIds(
            List<Long> deleteDailyMissionIds) {
        this.deleteDailyMissionIds = deleteDailyMissionIds;
        return this;
    }

    public UpdateDailyGoalRequestFixture withAddMissionIds(List<Long> addMissionIds) {
        this.addMissionIds = addMissionIds;
        return this;
    }

    public UpdateDailyGoalRequest build() {
        return new UpdateDailyGoalRequest(deleteDailyMissionIds, addMissionIds);
    }
}
