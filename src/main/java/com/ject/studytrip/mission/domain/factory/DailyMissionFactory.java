package com.ject.studytrip.mission.domain.factory;

import com.ject.studytrip.mission.domain.model.DailyMission;
import com.ject.studytrip.mission.domain.model.Mission;
import com.ject.studytrip.trip.domain.model.DailyGoal;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DailyMissionFactory {
    public static DailyMission create(Mission mission, DailyGoal dailyGoal) {
        return DailyMission.of(mission, dailyGoal);
    }
}
