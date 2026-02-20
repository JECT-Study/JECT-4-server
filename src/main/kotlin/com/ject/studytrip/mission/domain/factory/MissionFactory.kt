package com.ject.studytrip.mission.domain.factory

import com.ject.studytrip.mission.domain.model.Mission
import com.ject.studytrip.stamp.domain.model.Stamp

object MissionFactory {
    fun create(
        stamp: Stamp,
        name: String,
    ): Mission = Mission.of(stamp, name)
}
