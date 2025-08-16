package com.ject.studytrip.mission.fixture;

import com.ject.studytrip.mission.domain.factory.MissionFactory;
import com.ject.studytrip.mission.domain.model.Mission;
import com.ject.studytrip.stamp.domain.model.Stamp;
import org.springframework.test.util.ReflectionTestUtils;

public class MissionFixture {
    private static final String MISSION_NAME = "TEST MISSION NAME";

    public static Mission createMission(Stamp stamp) {
        return MissionFactory.create(stamp, MISSION_NAME);
    }

    public static Mission createMissionWithId(Long id, Stamp stamp) {
        Mission mission = MissionFactory.create(stamp, MISSION_NAME);
        ReflectionTestUtils.setField(mission, "id", id);

        return mission;
    }
}
