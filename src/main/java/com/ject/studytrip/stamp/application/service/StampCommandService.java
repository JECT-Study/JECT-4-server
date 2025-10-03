package com.ject.studytrip.stamp.application.service;

import com.ject.studytrip.stamp.domain.factory.StampFactory;
import com.ject.studytrip.stamp.domain.model.Stamp;
import com.ject.studytrip.stamp.domain.policy.StampPolicy;
import com.ject.studytrip.stamp.domain.repository.StampQueryRepository;
import com.ject.studytrip.stamp.domain.repository.StampRepository;
import com.ject.studytrip.stamp.presentation.dto.request.CreateStampRequest;
import com.ject.studytrip.stamp.presentation.dto.request.UpdateStampOrderRequest;
import com.ject.studytrip.stamp.presentation.dto.request.UpdateStampRequest;
import com.ject.studytrip.trip.domain.model.Trip;
import com.ject.studytrip.trip.domain.model.TripCategory;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StampCommandService {
    private final StampRepository stampRepository;
    private final StampQueryRepository stampQueryRepository;

    public Stamp createStamp(Trip trip, CreateStampRequest request) {
        Stamp newStamp = StampFactory.create(trip, request.name(), request.order());

        List<Stamp> existingStamps =
                stampRepository.findAllByTripIdAndDeletedAtIsNull(trip.getId());
        List<Stamp> combinedStamps = new ArrayList<>(existingStamps);
        combinedStamps.add(newStamp);

        StampPolicy.validateStampOrders(trip.getCategory(), combinedStamps);

        return stampRepository.save(newStamp);
    }

    public void createStamps(Trip trip, List<CreateStampRequest> requests) {
        List<Stamp> stamps =
                requests.stream()
                        .map(stamp -> StampFactory.create(trip, stamp.name(), stamp.order()))
                        .toList();

        StampPolicy.validateStampOrders(trip.getCategory(), stamps);

        stampRepository.saveAll(stamps);
    }

    public void updateStampName(Stamp stamp, UpdateStampRequest request) {
        stamp.updateName(request.name());
    }

    public void updateStampOrders(Trip trip, UpdateStampOrderRequest request) {
        // 배치 조회 (ID 목록 기준으로 조회하지만 순서는 보장되지 않음)
        List<Stamp> stamps = stampRepository.findAllByIdIn(request.orderedStampIds());

        StampPolicy.validateUpdateStampOrders(
                trip.getCategory(), request.orderedStampIds(), stamps);
        stamps.forEach(
                stamp -> {
                    StampPolicy.validateStampBelongsToTrip(trip.getId(), stamp);
                    StampPolicy.validateNotDeleted(stamp);
                });

        // 조회된 스탬프 ID 를 기준으로 매핑
        Map<Long, Stamp> stampMap =
                stamps.stream().collect(Collectors.toMap(Stamp::getId, Function.identity()));

        // 요청에서 전달된 ID 순서를 기준으로 스탬프 리스트 재정렬
        List<Stamp> orderedStamps = request.orderedStampIds().stream().map(stampMap::get).toList();

        int newOrder = 1;
        for (Stamp stamp : orderedStamps) {
            stamp.updateStampOrder(newOrder++);
        }
    }

    public void updateStampOrdersByTripCategoryChange(Long tripId, TripCategory newCategory) {
        List<Stamp> stamps = stampRepository.findAllByTripIdOrderByCreatedAtAsc(tripId);

        if (newCategory == TripCategory.EXPLORE) {
            stamps.forEach(stamp -> stamp.updateStampOrder(0));
            return;
        }

        int order = 1;
        for (Stamp stamp : stamps) {
            stamp.updateStampOrder(order++);
        }
    }

    public void deleteStamp(Long tripId, TripCategory tripCategory, Stamp stamp) {
        stamp.updateDeletedAt();

        if (tripCategory == TripCategory.COURSE) {
            shiftStampOrdersAfterDeleted(tripId, stamp.getStampOrder());
        }
    }

    public void completeStamp(Stamp stamp) {
        StampPolicy.validateCompleted(stamp);

        stamp.updateCompleted();
    }

    public void validateStampBelongsToTrip(Long tripId, Stamp stamp) {
        StampPolicy.validateStampBelongsToTrip(tripId, stamp);
    }

    public void validateAllStampsCompletedByTripId(Long tripId) {
        boolean exists =
                stampQueryRepository.existsByTripIdAndCompletedIsFalseAndDeletedAtIsNull(tripId);
        StampPolicy.validateAllCompleted(exists);
    }

    public long hardDeleteStamps() {
        return stampQueryRepository.deleteAllByDeletedAtIsNotNull();
    }

    public long hardDeleteStampsOwnedByDeletedTrip() {
        return stampQueryRepository.deleteAllByDeletedTripOwner();
    }

    private void shiftStampOrdersAfterDeleted(Long tripId, int deletedStampOrder) {
        List<Stamp> affectedStamps =
                stampQueryRepository.findStampsToShiftAfterOrder(tripId, deletedStampOrder);

        for (Stamp stamp : affectedStamps) {
            stamp.updateStampOrder(stamp.getStampOrder() - 1);
        }
    }
}
