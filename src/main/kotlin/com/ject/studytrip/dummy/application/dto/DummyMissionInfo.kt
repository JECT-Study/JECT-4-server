package com.ject.studytrip.dummy.application.dto

import com.ject.studytrip.mission.domain.model.Mission

data class DummyMissionInfo(
    val missionName: String,
    val completed: Boolean,
) {
    companion object {
        fun from(mission: Mission): DummyMissionInfo = DummyMissionInfo(mission.name, mission.isCompleted())
    }
}
