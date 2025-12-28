package com.ject.studytrip.trip.fixture

import com.ject.studytrip.member.domain.model.Member
import com.ject.studytrip.trip.domain.factory.TripFactory
import com.ject.studytrip.trip.domain.model.Trip
import com.ject.studytrip.trip.domain.model.TripCategory
import org.springframework.test.util.ReflectionTestUtils
import java.time.LocalDate

class TripFixture(
    private val member: Member,
    private val category: TripCategory,
) {
    var name: String = "TEST 여행 이름"
    var memo: String = "TEST 여행 메모"
    var endDate: LocalDate = LocalDate.now().plusDays(7)
    var totalStamps: Int = 1

    fun create(): Trip = TripFactory.create(member, name, memo, category, endDate, totalStamps)

    fun createWithId(id: Long): Trip =
        create().also {
            ReflectionTestUtils.setField(it, "id", id)
        }
}
