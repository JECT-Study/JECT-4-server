package com.ject.studytrip.dummy.application.facade

import com.ject.studytrip.dummy.application.dto.DummyStampInfo
import com.ject.studytrip.dummy.application.dto.DummyStampsInfo
import com.ject.studytrip.dummy.application.service.DummyStampCommandService
import com.ject.studytrip.dummy.application.service.DummyTripCommandService
import com.ject.studytrip.member.application.service.MemberQueryService
import org.springframework.stereotype.Component

@Component
class DummyStampFacade(
    // Query Service
    private val memberQueryService: MemberQueryService,
    // Command Service
    private val dummyTripCommandService: DummyTripCommandService,
    private val dummyStampCommandService: DummyStampCommandService,
) {
    fun generateDummyStamps(
        memberId: Long,
        category: String,
        count: Int,
    ): DummyStampsInfo {
        val member = memberQueryService.getValidMember(memberId)
        val trip = dummyTripCommandService.createDummyTrip(member, category, count)
        val stamps = (1..count).map { order -> dummyStampCommandService.createDummyStamp(trip, order) }

        return DummyStampsInfo(stamps.map { DummyStampInfo.from(it) })
    }
}
