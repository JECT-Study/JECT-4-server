package com.ject.studytrip.dummy.application.dto

import com.ject.studytrip.global.util.DateUtil
import com.ject.studytrip.stamp.domain.model.Stamp

data class DummyStampInfo(
    val stampName: String,
    val stampOrder: Int,
    val endDate: String?,
    val totalMissions: Int,
    val completedMissions: Int,
    val completed: Boolean,
) {
    companion object {
        @JvmStatic
        fun from(stamp: Stamp): DummyStampInfo =
            DummyStampInfo(
                stamp.name,
                stamp.stampOrder,
                DateUtil.formatDate(stamp.endDate),
                stamp.totalMissions,
                stamp.completedMissions,
                stamp.isCompleted,
            )
    }
}
