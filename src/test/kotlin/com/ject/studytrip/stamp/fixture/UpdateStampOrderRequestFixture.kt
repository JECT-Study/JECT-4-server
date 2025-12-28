package com.ject.studytrip.stamp.fixture

import com.ject.studytrip.stamp.presentation.dto.request.UpdateStampOrderRequest

class UpdateStampOrderRequestFixture {
    var orderedStampIds: List<Long> = listOf(1L, 2L)

    fun withOrderedStampIds(orderedStampIds: List<Long>): UpdateStampOrderRequestFixture = apply { this.orderedStampIds = orderedStampIds }

    fun build(): UpdateStampOrderRequest = UpdateStampOrderRequest(orderedStampIds)
}
