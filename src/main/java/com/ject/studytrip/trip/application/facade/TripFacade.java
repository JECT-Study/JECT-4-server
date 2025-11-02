package com.ject.studytrip.trip.application.facade;

import static com.ject.studytrip.global.common.constants.CacheNameConstants.*;

import com.ject.studytrip.member.application.service.MemberQueryService;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.stamp.application.dto.StampInfo;
import com.ject.studytrip.stamp.application.service.StampCommandService;
import com.ject.studytrip.stamp.application.service.StampQueryService;
import com.ject.studytrip.stamp.domain.model.Stamp;
import com.ject.studytrip.trip.application.dto.TripCategoryInfo;
import com.ject.studytrip.trip.application.dto.TripDetail;
import com.ject.studytrip.trip.application.dto.TripInfo;
import com.ject.studytrip.trip.application.dto.TripSliceInfo;
import com.ject.studytrip.trip.application.service.TripCommandService;
import com.ject.studytrip.trip.application.service.TripQueryService;
import com.ject.studytrip.trip.domain.model.Trip;
import com.ject.studytrip.trip.domain.model.TripCategory;
import com.ject.studytrip.trip.presentation.dto.request.CreateTripRequest;
import com.ject.studytrip.trip.presentation.dto.request.UpdateTripRequest;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class TripFacade {
    private final MemberQueryService memberQueryService;
    private final TripQueryService tripQueryService;
    private final StampQueryService stampQueryService;

    private final TripCommandService tripCommandService;
    private final StampCommandService stampCommandService;

    @Transactional(readOnly = true)
    public List<TripCategoryInfo> loadTripCategories() {
        return Arrays.stream(TripCategory.values()).map(TripCategoryInfo::from).toList();
    }

    @CacheEvict(cacheNames = TRIPS, allEntries = true)
    @Transactional
    public TripInfo createTrip(Long memberId, CreateTripRequest request) {
        Member member = memberQueryService.getValidMember(memberId);
        Trip trip = tripCommandService.createTrip(member, request);
        int nextOrder = stampQueryService.getNextStampOrderByTrip(trip);
        stampCommandService.createStamps(trip, nextOrder, request.stamps());

        return TripInfo.from(trip, null, null);
    }

    @Caching(
            evict = {
                @CacheEvict(cacheNames = TRIPS, allEntries = true),
                @CacheEvict(
                        cacheNames = TRIP,
                        key =
                                "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).trip(#memberId, #tripId)"),
                @CacheEvict(
                        cacheNames = STAMPS,
                        key =
                                "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).stamps(#memberId, #tripId)"),
                @CacheEvict(cacheNames = STAMP, allEntries = true)
            })
    @Transactional
    public void updateTrip(Long memberId, Long tripId, UpdateTripRequest request) {
        Member member = memberQueryService.getValidMember(memberId);
        Trip trip = tripQueryService.getValidTrip(member.getId(), tripId);

        tripCommandService.updateTrip(trip, request);

        if (request.category() != null)
            stampCommandService.updateStampOrdersByTripCategoryChange(
                    trip.getId(), TripCategory.from(request.category()));
    }

    @Caching(
            evict = {
                @CacheEvict(cacheNames = TRIPS, allEntries = true),
                @CacheEvict(
                        cacheNames = TRIP,
                        key =
                                "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).trip(#memberId, #tripId)"),
                @CacheEvict(
                        cacheNames = STAMPS,
                        key =
                                "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).stamps(#memberId, #tripId)")
            })
    @Transactional
    public void deleteTrip(Long memberId, Long tripId) {
        Member member = memberQueryService.getValidMember(memberId);
        Trip trip = tripQueryService.getValidTrip(member.getId(), tripId);

        tripCommandService.deleteTrip(trip);
    }

    @Cacheable(
            cacheNames = TRIPS,
            key =
                    "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).trips(#memberId, #page, #size)")
    @Transactional(readOnly = true)
    public TripSliceInfo getTripsByMember(Long memberId, int page, int size) {
        Slice<Trip> tripSlice = tripQueryService.getTripsSliceByMemberId(memberId, page, size);

        List<TripInfo> tripInfos =
                tripSlice.getContent().stream()
                        .map(
                                trip -> {
                                    Integer dDay = calculateDDay(trip.getEndDate());
                                    int progress =
                                            calculateProgress(
                                                    trip.getTotalStamps(),
                                                    trip.getCompletedStamps());
                                    return TripInfo.from(trip, dDay, progress);
                                })
                        .sorted(
                                Comparator.comparing(
                                        TripInfo::dDay,
                                        Comparator.nullsLast(Comparator.naturalOrder())))
                        .toList();

        return TripSliceInfo.of(tripInfos, tripSlice.hasNext());
    }

    @Cacheable(
            cacheNames = TRIP,
            key =
                    "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).trip(#memberId, #tripId)")
    @Transactional(readOnly = true)
    public TripDetail getTrip(Long memberId, Long tripId) {
        Member member = memberQueryService.getValidMember(memberId);
        Trip trip = tripQueryService.getValidTrip(member.getId(), tripId);

        int dDay = calculateDDay(trip.getEndDate());
        int progress = calculateProgress(trip.getTotalStamps(), trip.getCompletedStamps());

        List<Stamp> stamps = stampQueryService.getStampsByTripId(trip.getId());
        List<StampInfo> stampInfos = stamps.stream().map(StampInfo::from).toList();

        return TripDetail.from(TripInfo.from(trip, dDay, progress), stampInfos);
    }

    @Caching(
            evict = {
                @CacheEvict(cacheNames = TRIPS, allEntries = true),
                @CacheEvict(
                        cacheNames = TRIP,
                        key =
                                "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).trip(#memberId, #tripId)"),
                @CacheEvict(
                        cacheNames = STAMPS,
                        key =
                                "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).stamps(#memberId, #tripId)")
            })
    @Transactional
    public void completeTrip(Long memberId, Long tripId) {
        Member member = memberQueryService.getValidMember(memberId);
        Trip trip = tripQueryService.getValidTrip(member.getId(), tripId);

        stampCommandService.validateAllStampsCompletedByTripId(trip.getId());

        tripCommandService.completeTrip(trip);
    }

    private Integer calculateDDay(LocalDate endDate) {
        if (endDate == null) return null; // NULL 인 경우 무기한 여행

        LocalDate today = LocalDate.now();

        return (int) ChronoUnit.DAYS.between(today, endDate);
    }

    private int calculateProgress(int totalStamps, int completedStamps) {
        if (totalStamps == 0) return 0;

        double ratio = (double) completedStamps / totalStamps;

        return (int) (ratio * 100);
    }
}
