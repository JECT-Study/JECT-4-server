package com.ject.studytrip.stamp.fixture;

import com.ject.studytrip.stamp.presentation.dto.request.UpdateStampRequest;

public class UpdateStampRequestFixture {
    private String name = "TEST STAMP";

    public UpdateStampRequestFixture withName(String name) {
        this.name = name;
        return this;
    }

    public UpdateStampRequest buildUpdateName() {
        return new UpdateStampRequest(name);
    }
}
