package com.ject.studytrip.mission.fixture

import com.ject.studytrip.mission.presentation.dto.request.CreateMissionRequest

class CreateMissionRequestFixture(
    private val name: String = "TEST 미션 이름",
) {
    fun withName(name: String): CreateMissionRequestFixture = CreateMissionRequestFixture(name)

    fun build(): CreateMissionRequest = CreateMissionRequest(name)
}
