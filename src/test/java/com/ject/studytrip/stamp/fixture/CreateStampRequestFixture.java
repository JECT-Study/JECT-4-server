package com.ject.studytrip.stamp.fixture;

import com.ject.studytrip.stamp.presentation.dto.request.CreateStampRequest;
import java.time.LocalDate;

public class CreateStampRequestFixture {

    private String name = "TEST STAMP";
    private java.time.LocalDate endDate = java.time.LocalDate.now().plusDays(7);

    public CreateStampRequestFixture withName(String name) {
        this.name = name;
        return this;
    }

    public CreateStampRequestFixture withEndDateInPast() {
        this.endDate = LocalDate.now().minusDays(1);
        return this;
    }

    public CreateStampRequestFixture withEndDateAfterTripEndDate() {
        this.endDate = LocalDate.now().plusDays(100);
        return this;
    }

    public CreateStampRequest build() {
        return new CreateStampRequest(name, endDate);
    }
}
