package com.ject.studytrip.stamp.domain.policy;

import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.stamp.domain.error.StampErrorCode;
import com.ject.studytrip.stamp.domain.model.Stamp;
import com.ject.studytrip.trip.domain.model.TripCategory;
import java.time.LocalDate;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StampPolicy {
    public static void validateStampBelongsToTrip(Long tripId, Stamp stamp) {
        if (!stamp.getTrip().getId().equals(tripId))
            throw new CustomException(StampErrorCode.STAMP_NOT_BELONGS_TO_TRIP);
    }

    public static void validateNotDeleted(Stamp stamp) {
        if (stamp.getDeletedAt() != null)
            throw new CustomException(StampErrorCode.STAMP_ALREADY_DELETED);
    }

    public static void validateUpdateStampOrders(
            TripCategory tripCategory, List<Long> orderedStampIds, List<Stamp> savedStamps) {
        if (tripCategory == TripCategory.EXPLORE && !orderedStampIds.isEmpty())
            throw new CustomException(StampErrorCode.CANNOT_UPDATE_ORDER_FOR_EXPLORATION_TRIP);

        if (orderedStampIds.size() != savedStamps.size())
            throw new CustomException(StampErrorCode.INVALID_STAMP_ID_IN_REQUEST);
    }

    public static void validateStampListNotEmpty(List<Stamp> stamps) {
        if (stamps.isEmpty()) {
            throw new CustomException(StampErrorCode.STAMP_LIST_CANNOT_BE_EMPTY);
        }
    }

    public static void validateCompleted(Stamp stamp) {
        if (stamp.isCompleted()) {
            throw new CustomException(StampErrorCode.STAMP_ALREADY_COMPLETED);
        }
    }

    public static void validateAllCompleted(boolean exists) {
        if (exists) {
            throw new CustomException(StampErrorCode.ALL_STAMPS_NOT_COMPLETED);
        }
    }

    public static void validateEndDate(LocalDate tripEndDate, LocalDate stampEndDate) {
        if (stampEndDate == null) return;

        // 스탬프 종료일이 과거일 경우
        LocalDate today = LocalDate.now();
        if (stampEndDate.isBefore(today)) {
            throw new CustomException(StampErrorCode.STAMP_END_DATE_CANNOT_BE_IN_PAST);
        }

        if (tripEndDate == null) return;

        // 스탬프 종료일이 여행 종료일 이후일 경우
        if (stampEndDate.isAfter(tripEndDate)) {
            throw new CustomException(
                    StampErrorCode.STAMP_END_DATE_AFTER_TRIP_END_DATE_NOT_ALLOWED);
        }
    }
}
