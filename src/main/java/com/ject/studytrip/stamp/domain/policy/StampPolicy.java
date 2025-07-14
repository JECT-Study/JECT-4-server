package com.ject.studytrip.stamp.domain.policy;

import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.stamp.domain.error.StampErrorCode;
import com.ject.studytrip.stamp.domain.model.Stamp;
import com.ject.studytrip.trip.domain.model.TripCategory;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StampPolicy {
    public static void validateStampBelongsToTrip(Long tripId, Stamp stamp) {
        if (!stamp.getTrip().getId().equals(tripId))
            throw new CustomException(StampErrorCode.STAMP_NOT_BELONG_TO_TRIP);
    }

    public static void validateNotDeleted(Stamp stamp) {
        if (stamp.getDeletedAt() != null)
            throw new CustomException(StampErrorCode.STAMP_ALREADY_DELETED);
    }

    public static void validateStampDeadline(LocalDate tripEndDate, List<Stamp> stamps) {
        if (tripEndDate == null || stamps.isEmpty()) return;

        LocalDate today = LocalDate.now();
        for (Stamp stamp : stamps) {
            LocalDate stampDeadline = stamp.getDeadline();

            if (stampDeadline.isBefore(today))
                throw new CustomException(StampErrorCode.STAMP_DEADLINE_CANNOT_BE_IN_PAST);

            if (stampDeadline.isAfter(tripEndDate))
                throw new CustomException(StampErrorCode.STAMP_DEADLINE_EXCEEDS_TRIP_END_DATE);
        }
    }

    public static void validateStampOrders(TripCategory tripCategory, List<Stamp> stamps) {
        int maxOrder = stamps.size();
        Set<Integer> orderSet = new HashSet<>();

        for (Stamp stamp : stamps) {
            int order = stamp.getStampOrder();

            // 탐험형 여행이면서 순서가 존재할 경우
            if (tripCategory == TripCategory.EXPLORE && order > 0)
                throw new CustomException(StampErrorCode.INVALID_STAMP_ORDER_FOR_EXPLORATION_TRIP);

            if (tripCategory == TripCategory.COURSE) {
                // 코스형 여행이면서 순서가 1보다 작거나 총 개수보다 큰 경우
                if (order < 1 || order > maxOrder)
                    throw new CustomException(
                            StampErrorCode.INVALID_STAMP_ORDER_RANGE_FOR_COURSE_TRIP);

                // 코스형 여행이면서 중복된 순서일 경우
                if (!orderSet.add(order))
                    throw new CustomException(StampErrorCode.DUPLICATE_STAMP_ORDER_FOR_COURSE_TRIP);
            }
        }
    }

    public static void validateUpdateStampOrders(
            TripCategory tripCategory, List<Long> orderedStampIds, List<Stamp> savedStamps) {
        if (tripCategory == TripCategory.EXPLORE && !orderedStampIds.isEmpty())
            throw new CustomException(StampErrorCode.CANNOT_UPDATE_ORDER_FOR_EXPLORATION_TRIP);

        if (orderedStampIds.size() != savedStamps.size())
            throw new CustomException(StampErrorCode.INVALID_STAMP_ID_IN_REQUEST);
    }
}
