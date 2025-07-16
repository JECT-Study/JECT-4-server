package com.ject.studytrip.mission.fixture;

import com.ject.studytrip.mission.domain.factory.MissionFactory;
import com.ject.studytrip.mission.domain.model.Mission;
import com.ject.studytrip.stamp.domain.model.Stamp;
import org.springframework.test.util.ReflectionTestUtils;

public class MissionFixture {
    private static final String MISSION_NAME = "TEST MISSION NAME";
    private static final String MISSION_MEMO = "TEST MISSION MEMO";

    public static Mission createMission(Stamp stamp, int order) {
        return MissionFactory.create(stamp, MISSION_NAME, MISSION_MEMO, order);
    }

    public static Mission createMissionWithId(Long id, Stamp stamp, int order) {
        Mission mission = MissionFactory.create(stamp, MISSION_NAME, MISSION_MEMO, order);
        ReflectionTestUtils.setField(mission, "id", id);

        return mission;
    }
}
