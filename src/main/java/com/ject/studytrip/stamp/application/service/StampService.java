package com.ject.studytrip.stamp.application.service;

import com.ject.studytrip.stamp.domain.factory.StampFactory;
import com.ject.studytrip.stamp.domain.model.Stamp;
import com.ject.studytrip.stamp.domain.policy.StampPolicy;
import com.ject.studytrip.stamp.domain.repository.StampRepository;
import com.ject.studytrip.stamp.presentation.dto.request.CreateStampRequest;
import com.ject.studytrip.trip.domain.model.Trip;
import com.ject.studytrip.trip.domain.model.TripCategory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StampService {

    private final StampRepository stampRepository;

    public void createStamps(Trip trip, List<CreateStampRequest> requests) {
        List<Stamp> stamps =
                requests.stream()
                        .map(
                                stamp ->
                                        StampFactory.create(
                                                trip,
                                                stamp.name(),
                                                stamp.order(),
                                                stamp.deadline()))
                        .toList();

        StampPolicy.validateStampDeadline(trip.getEndDate(), stamps);
        StampPolicy.validateStampOrders(trip.getCategory(), stamps);

        stampRepository.saveAll(stamps);
    }

    public void updateStampsOrderByTripCategoryChange(Long tripId, TripCategory newCategory) {
        List<Stamp> stamps = stampRepository.findAllByTripIdOrderByDeadlineAsc(tripId);

        if (newCategory == TripCategory.EXPLORE) {
            stamps.forEach(stamp -> stamp.updateStampOrder(0));
            return;
        }

        int order = 1;
        for (Stamp stamp : stamps) {
            stamp.updateStampOrder(order++);
        }
    }

    public List<Stamp> getStampsByTripId(Long tripId) {
        return stampRepository.findAllByTripId(tripId);
    }
}
