package com.ject.studytrip.mission.helper;

import com.ject.studytrip.mission.domain.model.DailyMission;
import com.ject.studytrip.mission.domain.model.Mission;
import com.ject.studytrip.mission.domain.repository.DailyMissionRepository;
import com.ject.studytrip.mission.fixture.DailyMissionFixture;
import com.ject.studytrip.trip.domain.model.DailyGoal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DailyMissionTestHelper {

    @Autowired private DailyMissionRepository dailyMissionRepository;

    public DailyMission saveDailyMission(Mission mission, DailyGoal dailyGoal) {
        DailyMission dailyMission = DailyMissionFixture.createDailyMission(mission, dailyGoal);
        return dailyMissionRepository.save(dailyMission);
    }

    public DailyMission saveDeletedDailyMission(Mission mission, DailyGoal dailyGoal) {
        DailyMission dailyMission = DailyMissionFixture.createDailyMission(mission, dailyGoal);
        dailyMission.updateDeletedAt();

        return dailyMissionRepository.save(dailyMission);
    }
}
