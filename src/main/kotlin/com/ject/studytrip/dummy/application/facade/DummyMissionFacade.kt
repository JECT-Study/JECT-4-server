package com.ject.studytrip.dummy.application.facade

import com.ject.studytrip.dummy.application.dto.DummyMissionInfo
import com.ject.studytrip.dummy.application.dto.DummyMissionsInfo
import com.ject.studytrip.dummy.application.service.DummyMissionCommandService
import com.ject.studytrip.dummy.application.service.DummyStampCommandService
import com.ject.studytrip.dummy.application.service.DummyTripCommandService
import com.ject.studytrip.member.application.service.MemberQueryService
import org.springframework.stereotype.Component

@Component
class DummyMissionFacade(
    // Query Service
    private val memberQueryService: MemberQueryService,
    // Command Service
    private val dummyTripCommandService: DummyTripCommandService,
    private val dummyStampCommandService: DummyStampCommandService,
    private val dummyMissionCommandService: DummyMissionCommandService,
) {
    fun generateDummyMissions(
        memberId: Long,
        category: String,
        count: Int,
    ): DummyMissionsInfo {
        val member = memberQueryService.getValidMember(memberId)
        val trip = dummyTripCommandService.createDummyTrip(member, category, count)
        val stamp = dummyStampCommandService.createDummyStamp(trip, count)
        val missions = List(count) { dummyMissionCommandService.createDummyMission(stamp) }

        return DummyMissionsInfo.of(missions.map { DummyMissionInfo.from(it) })
    }
}
