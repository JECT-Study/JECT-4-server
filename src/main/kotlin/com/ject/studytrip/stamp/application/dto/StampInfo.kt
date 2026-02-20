package com.ject.studytrip.stamp.application.dto

import com.ject.studytrip.global.util.DateUtil
import com.ject.studytrip.global.util.EntityExtensions.requireId
import com.ject.studytrip.stamp.domain.model.Stamp

data class StampInfo(
    val stampId: Long,
    val stampName: String,
    val stampOrder: Int,
    val endDate: String?,
    val totalMissions: Int,
    val completedMissions: Int,
    val completed: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val deletedAt: String?,
) {
    companion object {
        fun from(stamp: Stamp): StampInfo =
            StampInfo(
                stamp.id.requireId(),
                stamp.name,
                stamp.stampOrder,
                stamp.endDate?.let { DateUtil.formatDate(it) },
                stamp.totalMissions,
                stamp.completedMissions,
                stamp.isCompleted(),
                DateUtil.formatDateTime(requireNotNull(stamp.createdAt)),
                DateUtil.formatDateTime(requireNotNull(stamp.updatedAt)),
                stamp.deletedAt?.let { DateUtil.formatDateTime(it) },
            )
    }
}
