package com.ject.studytrip.trip.fixture;

import com.ject.studytrip.trip.presentation.dto.request.UpdateTripRequest;
import java.time.LocalDate;

public class UpdateTripRequestFixture {

    private String name = null;
    private String memo = null;
    private String category = null;
    private LocalDate endDate = null;

    public UpdateTripRequestFixture withName(String name) {
        this.name = name;
        return this;
    }

    public UpdateTripRequestFixture withMemo(String memo) {
        this.memo = memo;
        return this;
    }

    public UpdateTripRequestFixture withEndDate(LocalDate endDate) {
        this.endDate = endDate;
        return this;
    }

    public UpdateTripRequestFixture withCategory(String category) {
        this.category = category;
        return this;
    }

    public UpdateTripRequest build() {
        return new UpdateTripRequest(name, memo, category, endDate);
    }
}
