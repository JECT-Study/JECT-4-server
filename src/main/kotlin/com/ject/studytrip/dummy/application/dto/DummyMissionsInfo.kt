package com.ject.studytrip.dummy.application.dto

data class DummyMissionsInfo(
    val missionInfos: List<DummyMissionInfo>,
) {
    companion object {
        @JvmStatic
        fun of(missionInfos: List<DummyMissionInfo>): DummyMissionsInfo = DummyMissionsInfo(missionInfos)
    }
}
