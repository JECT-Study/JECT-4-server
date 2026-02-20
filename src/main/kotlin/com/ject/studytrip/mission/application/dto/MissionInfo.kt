package com.ject.studytrip.mission.application.dto

import com.ject.studytrip.global.util.DateUtil
import com.ject.studytrip.global.util.EntityExtensions.requireId
import com.ject.studytrip.mission.domain.model.Mission

data class MissionInfo(
    val missionId: Long,
    val missionName: String,
    val completed: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val deletedAt: String?,
) {
    companion object {
        fun from(mission: Mission): MissionInfo =
            MissionInfo(
                mission.id.requireId(),
                mission.name,
                mission.isCompleted(),
                DateUtil.formatDateTime(requireNotNull(mission.createdAt)),
                DateUtil.formatDateTime(requireNotNull(mission.updatedAt)),
                mission.deletedAt?.let { DateUtil.formatDateTime(it) },
            )
    }
}
