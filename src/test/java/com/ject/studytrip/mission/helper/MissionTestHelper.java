package com.ject.studytrip.mission.helper;

import com.ject.studytrip.mission.domain.model.Mission;
import com.ject.studytrip.mission.domain.repository.MissionRepository;
import com.ject.studytrip.mission.fixture.MissionFixture;
import com.ject.studytrip.stamp.domain.model.Stamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MissionTestHelper {
    @Autowired private MissionRepository missionRepository;

    public Mission saveMission(Stamp stamp) {
        Mission mission = MissionFixture.createMission(stamp);

        return missionRepository.save(mission);
    }

    public Mission saveDeletedMission(Stamp stamp) {
        Mission mission = MissionFixture.createMission(stamp);
        mission.updateDeletedAt();

        return missionRepository.save(mission);
    }

    public Mission saveCompletedMission(Stamp stamp) {
        Mission mission = MissionFixture.createMission(stamp);
        mission.updateCompleted();

        return missionRepository.save(mission);
    }
}
