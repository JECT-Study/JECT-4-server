package com.ject.studytrip.trip.fixture;

import com.ject.studytrip.stamp.fixture.CreateStampRequestFixture;
import com.ject.studytrip.stamp.presentation.dto.request.CreateStampRequest;
import com.ject.studytrip.trip.domain.model.TripCategory;
import com.ject.studytrip.trip.presentation.dto.request.CreateTripRequest;
import java.time.LocalDate;
import java.util.List;

public class CreateTripRequestFixture {

    private String name = "TEST 여행";
    private String memo = "TEST 여행입니다.";
    private String category = TripCategory.COURSE.name();
    private LocalDate endDate = LocalDate.now().plusDays(10);
    private List<CreateStampRequest> stamps = List.of(new CreateStampRequestFixture().build());

    public CreateTripRequestFixture withName(String name) {
        this.name = name;
        return this;
    }

    public CreateTripRequestFixture withMemo(String memo) {
        this.memo = memo;
        return this;
    }

    public CreateTripRequestFixture withEndDate(LocalDate endDate) {
        this.endDate = endDate;
        return this;
    }

    public CreateTripRequestFixture withCategory(String category) {
        this.category = category;
        return this;
    }

    public CreateTripRequestFixture withStamps(List<CreateStampRequest> stamps) {
        this.stamps = stamps;
        return this;
    }

    public CreateTripRequest build() {
        return new CreateTripRequest(name, memo, category, endDate, stamps);
    }
}
