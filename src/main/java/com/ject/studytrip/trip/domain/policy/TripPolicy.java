package com.ject.studytrip.trip.domain.policy;

import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.trip.domain.error.TripErrorCode;
import com.ject.studytrip.trip.domain.model.Trip;
import com.ject.studytrip.trip.domain.model.TripCategory;
import com.ject.studytrip.trip.presentation.dto.request.CreateTripRequest;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TripPolicy {
    public static void validateOwner(Long memberId, Trip trip) {
        if (!trip.getMember().getId().equals(memberId)) {
            throw new CustomException(TripErrorCode.NOT_TRIP_OWNER);
        }
    }

    public static void validateEndDateByCategory(TripCategory category, LocalDate endDate) {
        if (category == TripCategory.EXPLORE) return;
        if (endDate == null) throw new CustomException(TripErrorCode.COURSE_TRIP_END_DATE_REQUIRED);
    }

    public static void validateEndDateIsNotBeforeStartDate(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) return;
        if (endDate.isBefore(startDate))
            throw new CustomException(TripErrorCode.TRIP_END_DATE_BEFORE_START_DATE);
    }

    public static void validateMinimumStamps(CreateTripRequest request) {
        if (request.stamps() == null || request.stamps().size() < 1)
            throw new CustomException(TripErrorCode.TRIP_STAMP_REQUIRED);
    }

    public static void validateNotDeleted(Trip trip) {
        if (trip.getDeletedAt() != null)
            throw new CustomException(TripErrorCode.TRIP_ALREADY_DELETED);
    }

    public static void validateCompleted(Trip trip) {
        if (trip.isCompleted()) {
            throw new CustomException(TripErrorCode.TRIP_ALREADY_COMPLETED);
        }
    }
}
