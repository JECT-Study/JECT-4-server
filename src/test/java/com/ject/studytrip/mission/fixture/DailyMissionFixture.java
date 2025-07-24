package com.ject.studytrip.mission.fixture;

import com.ject.studytrip.mission.domain.factory.DailyMissionFactory;
import com.ject.studytrip.mission.domain.model.DailyMission;
import com.ject.studytrip.mission.domain.model.Mission;
import com.ject.studytrip.trip.domain.model.DailyGoal;
import org.springframework.test.util.ReflectionTestUtils;

public class DailyMissionFixture {

    public static DailyMission createDailyMission(Mission mission, DailyGoal dailyGoal) {
        return DailyMissionFactory.create(mission, dailyGoal);
    }

    public static DailyMission createDailyMissionWithId(
            Long id, Mission mission, DailyGoal dailyGoal) {
        DailyMission dailyMission = DailyMissionFactory.create(mission, dailyGoal);
        ReflectionTestUtils.setField(dailyMission, "id", id);

        return dailyMission;
    }
}
