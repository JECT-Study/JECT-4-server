package com.ject.studytrip.stamp.application.facade;

import static com.ject.studytrip.global.common.constants.CacheNameConstants.*;

import com.ject.studytrip.mission.application.dto.MissionInfo;
import com.ject.studytrip.mission.application.service.MissionCommandService;
import com.ject.studytrip.mission.application.service.MissionQueryService;
import com.ject.studytrip.mission.domain.model.Mission;
import com.ject.studytrip.stamp.application.dto.StampDetail;
import com.ject.studytrip.stamp.application.dto.StampInfo;
import com.ject.studytrip.stamp.application.dto.StampsInfo;
import com.ject.studytrip.stamp.application.service.StampCommandService;
import com.ject.studytrip.stamp.application.service.StampQueryService;
import com.ject.studytrip.stamp.domain.model.Stamp;
import com.ject.studytrip.stamp.presentation.dto.request.CreateStampRequest;
import com.ject.studytrip.stamp.presentation.dto.request.UpdateStampOrderRequest;
import com.ject.studytrip.stamp.presentation.dto.request.UpdateStampRequest;
import com.ject.studytrip.trip.application.service.TripCommandService;
import com.ject.studytrip.trip.application.service.TripQueryService;
import com.ject.studytrip.trip.domain.model.Trip;
import com.ject.studytrip.trip.domain.model.TripCategory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class StampFacade {
    private final TripQueryService tripQueryService;
    private final StampQueryService stampQueryService;
    private final MissionQueryService missionQueryService;

    private final TripCommandService tripCommandService;
    private final StampCommandService stampCommandService;
    private final MissionCommandService missionCommandService;

    @Caching(
            evict = {
                @CacheEvict(
                        cacheNames = STAMPS,
                        key =
                                "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).stamps(#memberId, #tripId)"),
                @CacheEvict(
                        cacheNames = TRIP,
                        key =
                                "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).trip(#memberId, #tripId)"),
                @CacheEvict(cacheNames = TRIPS, allEntries = true)
            })
    @Transactional
    public StampInfo createStamp(Long memberId, Long tripId, CreateStampRequest request) {
        Trip trip = tripQueryService.getValidTrip(memberId, tripId);
        int nextOrder = stampQueryService.getNextStampOrderByTrip(trip);
        Stamp stamp = stampCommandService.createStamp(trip, nextOrder, request);
        tripCommandService.increaseTotalStamps(trip);

        return StampInfo.from(stamp);
    }

    @Caching(
            evict = {
                @CacheEvict(
                        cacheNames = STAMPS,
                        key =
                                "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).stamps(#memberId, #tripId)"),
                @CacheEvict(
                        cacheNames = STAMP,
                        key =
                                "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).stamp(#memberId, #tripId, #stampId)"),
                @CacheEvict(
                        cacheNames = TRIP,
                        key =
                                "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).trip(#memberId, #tripId)"),
                @CacheEvict(cacheNames = TRIPS, allEntries = true)
            })
    @Transactional
    public void updateStamp(Long memberId, Long tripId, Long stampId, UpdateStampRequest request) {
        Trip trip = tripQueryService.getValidTrip(memberId, tripId);
        Stamp stamp = stampQueryService.getValidStamp(trip.getId(), stampId);

        stampCommandService.updateStamp(trip, stamp, request);
    }

    @Caching(
            evict = {
                @CacheEvict(
                        cacheNames = STAMPS,
                        key =
                                "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).stamps(#memberId, #tripId)"),
                @CacheEvict(cacheNames = STAMP, allEntries = true),
                @CacheEvict(
                        cacheNames = TRIP,
                        key =
                                "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).trip(#memberId, #tripId)"),
                @CacheEvict(cacheNames = TRIPS, allEntries = true)
            })
    @Transactional
    public void updateStampOrders(Long memberId, Long tripId, UpdateStampOrderRequest request) {
        Trip trip = tripQueryService.getValidTrip(memberId, tripId);

        stampCommandService.updateStampOrders(trip, request);
    }

    @Caching(
            evict = {
                @CacheEvict(
                        cacheNames = STAMPS,
                        key =
                                "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).stamps(#memberId, #tripId)"),
                @CacheEvict(
                        cacheNames = STAMP,
                        key =
                                "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).stamp(#memberId, #tripId, #stampId)"),
                @CacheEvict(
                        cacheNames = TRIP,
                        key =
                                "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).trip(#memberId, #tripId)"),
                @CacheEvict(cacheNames = TRIPS, allEntries = true)
            })
    @Transactional
    public void deleteStamp(Long memberId, Long tripId, Long stampId) {
        Trip trip = tripQueryService.getValidTrip(memberId, tripId);
        Stamp stamp = stampQueryService.getValidStamp(trip.getId(), stampId);

        stampCommandService.deleteStamp(stamp);
        shiftStampOrdersIfTripCategoryIsCourse(trip, stamp.getStampOrder());
        tripCommandService.decreaseTotalStamps(trip);
    }

    @Cacheable(
            cacheNames = STAMPS,
            key =
                    "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).stamps(#memberId, #tripId)")
    @Transactional(readOnly = true)
    public StampsInfo getStampsByTrip(Long memberId, Long tripId) {
        Trip trip = tripQueryService.getValidTrip(memberId, tripId);
        List<Stamp> stamps = stampQueryService.getStampsByTripId(trip.getId());

        return StampsInfo.of(stamps.stream().map(StampInfo::from).toList());
    }

    @Cacheable(
            cacheNames = STAMP,
            key =
                    "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).stamp(#memberId, #tripId, #stampId)")
    @Transactional(readOnly = true)
    public StampDetail getStamp(Long memberId, Long tripId, Long stampId) {
        Trip trip = tripQueryService.getValidTrip(memberId, tripId);
        Stamp stamp = stampQueryService.getValidStamp(trip.getId(), stampId);
        List<Mission> missions = missionQueryService.getMissionsByStampId(stamp.getId());

        return StampDetail.from(
                StampInfo.from(stamp), missions.stream().map(MissionInfo::from).toList());
    }

    @Caching(
            evict = {
                @CacheEvict(
                        cacheNames = STAMP,
                        key =
                                "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).stamp(#memberId, #tripId, #stampId)"),
                @CacheEvict(
                        cacheNames = STAMPS,
                        key =
                                "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).stamps(#memberId, #tripId)"),
                @CacheEvict(
                        cacheNames = TRIP,
                        key =
                                "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).trip(#memberId, #tripId)"),
                @CacheEvict(cacheNames = TRIPS, allEntries = true)
            })
    @Transactional
    public void completeStamp(Long memberId, Long tripId, Long stampId) {
        Trip trip = tripQueryService.getValidTrip(memberId, tripId);
        Stamp stamp = stampQueryService.getValidStamp(trip.getId(), stampId);

        missionCommandService.validateAllMissionsCompletedByStampId(stamp.getId());

        stampCommandService.completeStamp(stamp);
        tripCommandService.increaseCompletedStamps(trip);
    }

    private void shiftStampOrdersIfTripCategoryIsCourse(Trip trip, int stampOrder) {
        if (trip.getCategory() != TripCategory.COURSE) return;

        List<Stamp> affectedStamps =
                stampQueryService.getStampsToShiftAfterDeleted(trip.getId(), stampOrder);

        stampCommandService.shiftStampOrders(affectedStamps);
    }
}
