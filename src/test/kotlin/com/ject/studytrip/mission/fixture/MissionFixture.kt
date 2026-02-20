package com.ject.studytrip.mission.fixture

import com.ject.studytrip.mission.domain.factory.MissionFactory
import com.ject.studytrip.mission.domain.model.Mission
import com.ject.studytrip.stamp.domain.model.Stamp
import org.springframework.test.util.ReflectionTestUtils

class MissionFixture(
    private val stamp: Stamp,
    private val name: String = "TEST 미션 이름",
) {
    fun create(): Mission = MissionFactory.create(stamp, name)

    fun createWithId(id: Long): Mission =
        create().also {
            ReflectionTestUtils.setField(it, "id", id)
        }
}
