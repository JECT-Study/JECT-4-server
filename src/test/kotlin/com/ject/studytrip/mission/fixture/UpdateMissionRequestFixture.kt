package com.ject.studytrip.mission.fixture

import com.ject.studytrip.mission.presentation.dto.request.UpdateMissionRequest

class UpdateMissionRequestFixture {
    var name: String = "TEST 새로운 미션 이름"

    fun withName(name: String): UpdateMissionRequestFixture = apply { this.name = name }

    fun build(): UpdateMissionRequest = UpdateMissionRequest(name)
}
