package com.ject.studytrip.dummy.application.service;

import com.ject.studytrip.dummy.application.dto.CreateDummyMissionCommand;
import com.ject.studytrip.dummy.application.generator.CreateDummyMissionCommandGenerator;
import com.ject.studytrip.mission.domain.factory.MissionFactory;
import com.ject.studytrip.mission.domain.model.Mission;
import com.ject.studytrip.stamp.domain.model.Stamp;
import org.springframework.stereotype.Service;

@Service
public class DummyMissionCommandService {

    public Mission createDummyMission(Stamp stamp) {
        CreateDummyMissionCommand command = CreateDummyMissionCommandGenerator.of();

        return MissionFactory.create(stamp, command.name());
    }
}
