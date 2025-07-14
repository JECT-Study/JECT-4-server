package com.ject.studytrip.stamp.application.facade;

import com.ject.studytrip.mission.application.dto.MissionInfo;
import com.ject.studytrip.mission.application.service.MissionService;
import com.ject.studytrip.stamp.application.dto.StampDetail;
import com.ject.studytrip.stamp.application.dto.StampInfo;
import com.ject.studytrip.stamp.application.service.StampService;
import com.ject.studytrip.stamp.domain.model.Stamp;
import com.ject.studytrip.stamp.presentation.dto.request.CreateStampRequest;
import com.ject.studytrip.stamp.presentation.dto.request.UpdateStampNameAndDeadlineRequest;
import com.ject.studytrip.stamp.presentation.dto.request.UpdateStampOrderRequest;
import com.ject.studytrip.trip.application.service.TripService;
import com.ject.studytrip.trip.domain.model.Trip;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StampFacade {
    private final TripService tripService;
    private final StampService stampService;
    private final MissionService missionService;

    @Transactional
    public StampInfo createStamp(Long memberId, Long tripId, CreateStampRequest request) {
        Trip trip = tripService.getValidTrip(memberId, tripId);
        Stamp stamp = stampService.createStamp(trip, request);

        tripService.increaseTotalStamps(trip);

        return StampInfo.from(stamp);
    }

    @Transactional
    public void updateStampNameAndDeadline(
            Long memberId, Long tripId, Long stampId, UpdateStampNameAndDeadlineRequest request) {
        Trip trip = tripService.getValidTrip(memberId, tripId);
        Stamp stamp = stampService.getValidStamp(trip.getId(), stampId);

        stampService.updateStampNameAndDeadline(trip, stamp, request);
    }

    @Transactional
    public void updateStampOrders(Long memberId, Long tripId, UpdateStampOrderRequest request) {
        Trip trip = tripService.getValidTrip(memberId, tripId);

        stampService.updateStampsOrders(trip, request);
    }

    @Transactional
    public void deleteStamp(Long memberId, Long tripId, Long stampId) {
        Trip trip = tripService.getValidTrip(memberId, tripId);
        Stamp stamp = stampService.getValidStamp(trip.getId(), stampId);

        stampService.deleteStamp(trip.getId(), trip.getCategory(), stamp);
        tripService.decreaseTotalStamps(trip);

        // TODO : 추후 삭제로직 업데이트 (연쇄 삭제 처리)
    }

    public List<StampInfo> getStampsByTrip(Long memberId, Long tripId) {
        Trip trip = tripService.getValidTrip(memberId, tripId);
        List<Stamp> stamps = stampService.getStampsByTripId(trip.getId());

        return stamps.stream().map(StampInfo::from).toList();
    }

    public StampDetail getStamp(Long memberId, Long tripId, Long stampId) {
        Trip trip = tripService.getValidTrip(memberId, tripId);
        Stamp stamp = stampService.getValidStamp(trip.getId(), stampId);
        List<MissionInfo> missionInfos = missionService.getMissionsByStamp(stamp.getId());

        return StampDetail.from(StampInfo.from(stamp), missionInfos);
    }
}
