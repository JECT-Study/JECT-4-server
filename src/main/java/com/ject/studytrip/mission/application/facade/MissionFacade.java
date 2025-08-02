package com.ject.studytrip.mission.application.facade;

import com.ject.studytrip.mission.application.dto.MissionInfo;
import com.ject.studytrip.mission.application.service.MissionService;
import com.ject.studytrip.mission.domain.model.Mission;
import com.ject.studytrip.mission.presentation.dto.request.CreateMissionRequest;
import com.ject.studytrip.mission.presentation.dto.request.UpdateMissionOrderRequest;
import com.ject.studytrip.mission.presentation.dto.request.UpdateMissionRequest;
import com.ject.studytrip.stamp.application.service.StampService;
import com.ject.studytrip.stamp.domain.model.Stamp;
import com.ject.studytrip.trip.application.service.TripService;
import com.ject.studytrip.trip.domain.model.Trip;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MissionFacade {
    private final TripService tripService;
    private final StampService stampService;
    private final MissionService missionService;

    public MissionInfo createMission(
            Long memberId, Long tripId, Long stampId, CreateMissionRequest request) {
        Stamp stamp = getValidStampFromTripOwnedByMember(memberId, tripId, stampId);
        Mission mission = missionService.createMission(stamp, request);

        return MissionInfo.from(mission);
    }

    public void updateMissionNameAndMemoIfPresent(
            Long memberId,
            Long tripId,
            Long stampId,
            Long missionId,
            UpdateMissionRequest request) {
        Stamp stamp = getValidStampFromTripOwnedByMember(memberId, tripId, stampId);
        Mission mission = missionService.getValidMission(stamp.getId(), missionId);

        missionService.updateMissionNameAndMemoIfPresent(stamp.getId(), mission, request);
    }

    public void updateMissionOrders(
            Long memberId, Long tripId, Long stampId, UpdateMissionOrderRequest request) {
        Stamp stamp = getValidStampFromTripOwnedByMember(memberId, tripId, stampId);

        missionService.updateMissionOrders(stamp.getId(), request);
    }

    public void deleteMission(Long memberId, Long tripId, Long stampId, Long missionId) {
        Stamp stamp = getValidStampFromTripOwnedByMember(memberId, tripId, stampId);
        Mission mission = missionService.getValidMission(stamp.getId(), missionId);

        missionService.deleteMission(stamp.getId(), mission);
    }

    public List<MissionInfo> getMissionsByStamp(Long memberId, Long tripId, Long stampId) {
        Stamp stamp = getValidStampFromTripOwnedByMember(memberId, tripId, stampId);
        List<Mission> missions = missionService.getMissionsByStampId(stamp.getId());

        return missions.stream().map(MissionInfo::from).toList();
    }

    private Stamp getValidStampFromTripOwnedByMember(Long memberId, Long tripId, Long stampId) {
        Trip trip = tripService.getValidTrip(memberId, tripId);

        return stampService.getValidStamp(trip.getId(), stampId);
    }
}
