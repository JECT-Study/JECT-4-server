package com.ject.studytrip.stamp.fixture;

import com.ject.studytrip.stamp.presentation.dto.request.UpdateStampNameAndDeadlineRequest;
import com.ject.studytrip.stamp.presentation.dto.request.UpdateStampOrderRequest;
import java.time.LocalDate;
import java.util.List;

public class UpdateStampRequestFixture {
    private String name = "TEST STAMP";
    private List<Long> orderedStampIds = List.of(1L, 2L);
    private LocalDate deadline = LocalDate.now().plusDays(1);

    public UpdateStampRequestFixture withName(String name) {
        this.name = name;
        return this;
    }

    public UpdateStampRequestFixture withOrderedStampIds(List<Long> orderedStampIds) {
        this.orderedStampIds = orderedStampIds;
        return this;
    }

    public UpdateStampRequestFixture withDeadline(LocalDate deadline) {
        this.deadline = deadline;
        return this;
    }

    public UpdateStampNameAndDeadlineRequest buildUpdateNameAndDeadline() {
        return new UpdateStampNameAndDeadlineRequest(name, deadline);
    }

    public UpdateStampOrderRequest buildUpdateOrders() {
        return new UpdateStampOrderRequest(orderedStampIds);
    }
}
