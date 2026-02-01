package com.ject.studytrip.dummy.application.service

import com.ject.studytrip.dummy.application.dto.CreateDummyMissionCommand
import com.ject.studytrip.mission.domain.factory.MissionFactory
import com.ject.studytrip.mission.domain.model.Mission
import com.ject.studytrip.stamp.domain.model.Stamp
import org.springframework.stereotype.Service

@Service
class DummyMissionCommandService {
    fun createDummyMission(stamp: Stamp): Mission {
        val command = CreateDummyMissionCommand.of("testMission")

        return MissionFactory.create(stamp, command.name)
    }
}
