package com.ject.studytrip.stamp.fixture;

import com.ject.studytrip.stamp.presentation.dto.request.UpdateStampOrderRequest;
import com.ject.studytrip.stamp.presentation.dto.request.UpdateStampRequest;
import java.util.List;

public class UpdateStampRequestFixture {
    private String name = "TEST STAMP";
    private List<Long> orderedStampIds = List.of(1L, 2L);

    public UpdateStampRequestFixture withName(String name) {
        this.name = name;
        return this;
    }

    public UpdateStampRequestFixture withOrderedStampIds(List<Long> orderedStampIds) {
        this.orderedStampIds = orderedStampIds;
        return this;
    }

    public UpdateStampRequest buildUpdateName() {
        return new UpdateStampRequest(name);
    }

    public UpdateStampOrderRequest buildUpdateOrders() {
        return new UpdateStampOrderRequest(orderedStampIds);
    }
}
