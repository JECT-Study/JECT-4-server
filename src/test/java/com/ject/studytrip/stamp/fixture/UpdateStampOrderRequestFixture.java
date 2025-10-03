package com.ject.studytrip.stamp.fixture;

import com.ject.studytrip.stamp.presentation.dto.request.UpdateStampOrderRequest;
import java.util.List;

public class UpdateStampOrderRequestFixture {
    private List<Long> orderedStampIds = List.of(1L, 2L);

    public UpdateStampOrderRequest buildUpdateOrders() {
        return new UpdateStampOrderRequest(orderedStampIds);
    }

    public UpdateStampOrderRequestFixture withOrderedStampIds(List<Long> orderedStampIds) {
        this.orderedStampIds = orderedStampIds;
        return this;
    }
}
