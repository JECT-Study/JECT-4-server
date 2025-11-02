package com.ject.studytrip.trip.application.service;

import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.trip.domain.factory.TripFactory;
import com.ject.studytrip.trip.domain.model.Trip;
import com.ject.studytrip.trip.domain.model.TripCategory;
import com.ject.studytrip.trip.domain.policy.TripPolicy;
import com.ject.studytrip.trip.domain.repository.TripCommandRepository;
import com.ject.studytrip.trip.domain.repository.TripRepository;
import com.ject.studytrip.trip.presentation.dto.request.CreateTripRequest;
import com.ject.studytrip.trip.presentation.dto.request.UpdateTripRequest;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TripCommandService {
    private final TripRepository tripRepository;
    private final TripCommandRepository tripCommandRepository;

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

    public void updateTrip(Trip trip, UpdateTripRequest request) {
        TripCategory category = null;
        if (request.category() != null) category = TripCategory.from(request.category());

        TripPolicy.validateEndDateIsNotBeforeStartDate(trip.getStartDate(), request.endDate());

        trip.update(request.name(), request.memo(), category, request.endDate());
    }

    public void increaseTotalStamps(Trip trip) {
        trip.increaseTotalStamps();
    }

    public void decreaseTotalStamps(Trip trip) {
        trip.decreaseTotalStamps();
    }

    public void deleteTrip(Trip trip) {
        trip.updateDeletedAt();
    }

    public void completeTrip(Trip trip) {
        TripPolicy.validateCompleted(trip);

        trip.updateCompleted();
    }

    public void increaseCompletedStamps(Trip trip) {
        trip.increaseCompletedStamps();
    }

    public long hardDeleteTrips() {
        return tripCommandRepository.deleteAllByDeletedAtIsNotNull();
    }

    public long hardDeleteTripsOwnedByDeletedMember() {
        return tripCommandRepository.deleteAllByDeletedMemberOwner();
    }

    public long hardDeleteTripsByMember(Long memberId) {
        return tripCommandRepository.deleteAllByMemberId(memberId);
    }
}
