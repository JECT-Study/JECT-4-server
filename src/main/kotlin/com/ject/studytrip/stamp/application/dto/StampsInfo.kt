package com.ject.studytrip.stamp.application.dto

data class StampsInfo(
    val stampInfos: List<StampInfo>,
) {
    companion object {
        @JvmStatic
        fun of(stampInfos: List<StampInfo>): StampsInfo = StampsInfo(stampInfos)
    }
}
