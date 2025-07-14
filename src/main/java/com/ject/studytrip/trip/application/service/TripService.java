package com.ject.studytrip.trip.application.service;

import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.trip.domain.error.TripErrorCode;
import com.ject.studytrip.trip.domain.factory.TripFactory;
import com.ject.studytrip.trip.domain.model.Trip;
import com.ject.studytrip.trip.domain.model.TripCategory;
import com.ject.studytrip.trip.domain.policy.TripPolicy;
import com.ject.studytrip.trip.domain.repository.TripQueryRepository;
import com.ject.studytrip.trip.domain.repository.TripRepository;
import com.ject.studytrip.trip.presentation.dto.request.CreateTripRequest;
import com.ject.studytrip.trip.presentation.dto.request.UpdateTripRequest;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TripService {
    private final TripRepository tripRepository;
    private final TripQueryRepository tripQueryRepository;

    public Trip createTrip(Member member, CreateTripRequest request) {
        TripCategory category = TripCategory.from(request.category());

        TripPolicy.validateEndDateByCategory(category, request.endDate());
        TripPolicy.validateEndDateIsNotBeforeStartDate(LocalDate.now(), request.endDate());
        TripPolicy.validateMinimumStamps(request);

        Trip trip =
                TripFactory.create(
                        member,
                        request.name(),
                        request.memo(),
                        category,
                        request.endDate(),
                        request.stamps().size());
        return tripRepository.save(trip);
    }

    public void updateTrip(Long memberId, Trip trip, UpdateTripRequest request) {
        TripCategory category = null;
        if (request.category() != null) category = TripCategory.from(request.category());

        TripPolicy.validateOwner(memberId, trip);
        TripPolicy.validateEndDateIsNotBeforeStartDate(trip.getStartDate(), request.endDate());
        TripPolicy.validateNotDeleted(trip);

        trip.update(request.name(), request.memo(), category, request.endDate());
    }

    public void increaseTotalStamps(Trip trip) {
        trip.increaseTotalStamps();
    }

    public void decreaseTotalStamps(Trip trip) {
        trip.decreaseTotalStamps();
    }

    public void deleteTrip(Long memberId, Trip trip) {
        TripPolicy.validateOwner(memberId, trip);

        trip.updateDeletedAt();

        // TODO : 삭제는 로직을 더 구상해본 후 추후 리팩토링
    }

    public Trip getTrip(Long tripId) {
        Trip trip =
                tripRepository
                        .findById(tripId)
                        .orElseThrow(() -> new CustomException(TripErrorCode.TRIP_NOT_FOUND));

        TripPolicy.validateNotDeleted(trip);

        return trip;
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
}
