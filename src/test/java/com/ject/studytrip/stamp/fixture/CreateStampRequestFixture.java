package com.ject.studytrip.stamp.fixture;

import com.ject.studytrip.stamp.presentation.dto.request.CreateStampRequest;
import java.time.LocalDate;

public class CreateStampRequestFixture {

    private String name = "TEST STAMP";
    private int stampOrder = 1;
    private LocalDate deadline = LocalDate.now().plusDays(7);

    public CreateStampRequestFixture withName(String name) {
        this.name = name;
        return this;
    }

    public CreateStampRequestFixture withStampOrder(int stampOrder) {
        this.stampOrder = stampOrder;
        return this;
    }

    public CreateStampRequestFixture withDeadline(LocalDate deadline) {
        this.deadline = deadline;
        return this;
    }

    public CreateStampRequest build() {
        return new CreateStampRequest(name, stampOrder, deadline);
    }
}
