package com.ject.studytrip.trip.application.facade;

import com.ject.studytrip.member.application.service.MemberService;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.stamp.application.dto.StampInfo;
import com.ject.studytrip.stamp.application.service.StampService;
import com.ject.studytrip.stamp.domain.model.Stamp;
import com.ject.studytrip.trip.application.dto.TripCategoryInfo;
import com.ject.studytrip.trip.application.dto.TripDetail;
import com.ject.studytrip.trip.application.dto.TripInfo;
import com.ject.studytrip.trip.application.service.TripService;
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
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TripFacade {
    private final TripService tripService;
    private final StampService stampService;
    private final MemberService memberService;

    public List<TripCategoryInfo> loadTripCategories() {
        return Arrays.stream(TripCategory.values()).map(TripCategoryInfo::from).toList();
    }

    @Transactional
    public TripInfo createTrip(Long memberId, CreateTripRequest request) {
        Member member = memberService.getMember(memberId);
        Trip trip = tripService.createTrip(member, request);
        stampService.createStamps(trip, request.stamps());

        return TripInfo.from(trip, null, null);
    }

    @Transactional
    public void updateTrip(Long memberId, Long tripId, UpdateTripRequest request) {
        Member member = memberService.getMember(memberId);
        Trip trip = tripService.getValidTrip(member.getId(), tripId);

        tripService.updateTrip(member.getId(), trip, request);

        if (request.category() != null)
            stampService.updateStampOrdersByTripCategoryChange(
                    trip.getId(), TripCategory.from(request.category()));
    }

    @Transactional
    public void deleteTrip(Long memberId, Long tripId) {
        Member member = memberService.getMember(memberId);
        Trip trip = tripService.getValidTrip(member.getId(), tripId);
        tripService.deleteTrip(member.getId(), trip);

        // TODO : 추후 엔티티가 생성되면, 여행과 관련된 엔티티를 모두 soft delete 하는 로직 추가
    }

    public Slice<TripInfo> getTripsByMember(Long memberId, int page, int size) {
        Slice<Trip> tripSlice = tripService.getTripsSliceByMemberId(memberId, page, size);

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

        return new SliceImpl<>(tripInfos, tripSlice.getPageable(), tripSlice.hasNext());
    }

    public TripDetail getTrip(Long memberId, Long tripId) {
        Member member = memberService.getMember(memberId);
        Trip trip = tripService.getValidTrip(member.getId(), tripId);

        int dDay = calculateDDay(trip.getEndDate());
        int progress = calculateProgress(trip.getTotalStamps(), trip.getCompletedStamps());

        List<Stamp> stamps = stampService.getStampsByTripId(trip.getId());
        List<StampInfo> stampInfos = stamps.stream().map(StampInfo::from).toList();

        return TripDetail.from(TripInfo.from(trip, dDay, progress), stampInfos);
    }

    public void completeTrip(Long memberId, Long tripId) {
        Member member = memberService.getMember(memberId);
        Trip trip = tripService.getValidTrip(member.getId(), tripId);

        stampService.validateAllStampsCompletedByTripId(trip.getId());

        tripService.completeTrip(trip);
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
