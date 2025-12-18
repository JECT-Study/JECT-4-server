package com.ject.studytrip.mission.application.dto

import com.ject.studytrip.global.util.DateUtil
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
        @JvmStatic
        fun from(mission: Mission): MissionInfo =
            MissionInfo(
                mission.getId(),
                mission.getName(),
                mission.isCompleted(),
                DateUtil.formatDateTime(mission.getCreatedAt()),
                DateUtil.formatDateTime(mission.getUpdatedAt()),
                mission.getDeletedAt()?.let { DateUtil.formatDateTime(it) },
            )
    }
}
