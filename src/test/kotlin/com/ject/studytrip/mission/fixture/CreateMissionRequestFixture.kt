package com.ject.studytrip.mission.fixture

import com.ject.studytrip.mission.presentation.dto.request.CreateMissionRequest

class CreateMissionRequestFixture {
    var name: String = "TEST 미션 이름"

    fun withName(name: String): CreateMissionRequestFixture = apply { this.name = name }

    fun build(): CreateMissionRequest = CreateMissionRequest(name)
}
