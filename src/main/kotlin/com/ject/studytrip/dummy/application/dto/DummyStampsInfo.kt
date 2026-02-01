package com.ject.studytrip.dummy.application.dto

data class DummyStampsInfo(
    val stampsInfos: List<DummyStampInfo>,
) {
    companion object {
        @JvmStatic
        fun of(stampsInfos: List<DummyStampInfo>): DummyStampsInfo = DummyStampsInfo(stampsInfos)
    }
}
