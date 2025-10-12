package com.ject.studytrip.trip.application.service;

import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.trip.application.dto.TripCount;
import com.ject.studytrip.trip.domain.error.TripErrorCode;
import com.ject.studytrip.trip.domain.model.Trip;
import com.ject.studytrip.trip.domain.model.TripCategory;
import com.ject.studytrip.trip.domain.policy.TripPolicy;
import com.ject.studytrip.trip.domain.repository.TripQueryRepository;
import com.ject.studytrip.trip.domain.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TripQueryService {
    private final TripRepository tripRepository;
    private final TripQueryRepository tripQueryRepository;

    public Trip getTrip(Long tripId) {
        return tripRepository
                .findById(tripId)
                .orElseThrow(() -> new CustomException(TripErrorCode.TRIP_NOT_FOUND));
    }

    public Trip getValidTrip(Long memberId, Long tripId) {
        Trip trip =
                tripRepository
                        .findById(tripId)
                        .orElseThrow(() -> new CustomException(TripErrorCode.TRIP_NOT_FOUND));

        TripPolicy.validateOwner(memberId, trip);
        TripPolicy.validateNotDeleted(trip);

        return trip;
    }

    public Slice<Trip> getTripsSliceByMemberId(Long memberId, int page, int size) {
        return tripQueryRepository.findSliceByMemberId(memberId, PageRequest.of(page, size));
    }

    public TripCount getActiveTripCountsByMemberId(Long memberId) {
        long courseCount =
                tripQueryRepository.countActiveTripsByMemberIdAndCategory(
                        memberId, TripCategory.COURSE);
        long exploreCount =
                tripQueryRepository.countActiveTripsByMemberIdAndCategory(
                        memberId, TripCategory.EXPLORE);

        return TripCount.of(courseCount, exploreCount);
    }

    public Trip getValidCompletedTrip(Long memberId, Long tripId) {
        Trip trip =
                tripRepository
                        .findById(tripId)
                        .orElseThrow(() -> new CustomException(TripErrorCode.TRIP_NOT_FOUND));

        TripPolicy.validateOwner(memberId, trip);
        TripPolicy.validateNotDeleted(trip);
        TripPolicy.validateNotCompleted(trip);

        return trip;
    }
}
