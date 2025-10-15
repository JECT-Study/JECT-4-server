package com.ject.studytrip.stamp.fixture;

import com.ject.studytrip.stamp.presentation.dto.request.UpdateStampRequest;
import java.time.LocalDate;

public class UpdateStampRequestFixture {
    private String name = "TEST STAMP";
    private LocalDate endDate = LocalDate.now().plusDays(7);

    public UpdateStampRequestFixture withName(String name) {
        this.name = name;
        return this;
    }

    public UpdateStampRequestFixture withEndDateInPast() {
        this.endDate = LocalDate.now().minusDays(1);
        return this;
    }

    public UpdateStampRequestFixture withEndDateAfterTripEndDate() {
        this.endDate = LocalDate.now().plusDays(100);
        return this;
    }

    public UpdateStampRequest buildUpdateName() {
        return new UpdateStampRequest(name, null);
    }

    public UpdateStampRequest buildUpdateEndDate() {
        return new UpdateStampRequest(null, endDate);
    }
}
