package com.ject.studytrip.trip.domain.factory;

import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.trip.domain.model.Trip;
import com.ject.studytrip.trip.domain.model.TripCategory;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TripFactory {
    public static Trip create(
            Member member,
            String name,
            String memo,
            TripCategory category,
            LocalDate endDate,
            int totalStamps) {
        return Trip.of(member, name, memo, category, endDate, totalStamps);
    }
}
