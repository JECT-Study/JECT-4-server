package com.ject.studytrip.mission.application.facade;

import static com.ject.studytrip.global.common.constants.CacheNameConstants.*;

import com.ject.studytrip.mission.application.dto.MissionInfo;
import com.ject.studytrip.mission.application.service.MissionCommandService;
import com.ject.studytrip.mission.application.service.MissionQueryService;
import com.ject.studytrip.mission.domain.model.Mission;
import com.ject.studytrip.mission.presentation.dto.request.CreateMissionRequest;
import com.ject.studytrip.mission.presentation.dto.request.UpdateMissionRequest;
import com.ject.studytrip.stamp.application.service.StampQueryService;
import com.ject.studytrip.stamp.domain.model.Stamp;
import com.ject.studytrip.trip.application.service.TripQueryService;
import com.ject.studytrip.trip.domain.model.Trip;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class MissionFacade {
    private final TripQueryService tripQueryService;
    private final StampQueryService stampQueryService;
    private final MissionQueryService missionQueryService;

    private final MissionCommandService missionCommandService;

    @Caching(
            evict = {
                @CacheEvict(
                        cacheNames = MISSIONS,
                        key =
                                "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).missions(#memberId, #tripId, #stampId)"),
                @CacheEvict(
                        cacheNames = STAMP,
                        key =
                                "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).stamp(#memberId, #tripId, #stampId)")
            })
    @Transactional
    public MissionInfo createMission(
            Long memberId, Long tripId, Long stampId, CreateMissionRequest request) {
        Stamp stamp = getValidStampFromTripOwnedByMember(memberId, tripId, stampId);
        Mission mission = missionCommandService.createMission(stamp, request);

        return MissionInfo.from(mission);
    }

    @Caching(
            evict = {
                @CacheEvict(
                        cacheNames = MISSIONS,
                        key =
                                "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).missions(#memberId, #tripId, #stampId)"),
                @CacheEvict(
                        cacheNames = STAMP,
                        key =
                                "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).stamp(#memberId, #tripId, #stampId)")
            })
    @Transactional
    public void updateMissionNameIfPresent(
            Long memberId,
            Long tripId,
            Long stampId,
            Long missionId,
            UpdateMissionRequest request) {
        Stamp stamp = getValidStampFromTripOwnedByMember(memberId, tripId, stampId);
        Mission mission = missionQueryService.getValidMission(stamp.getId(), missionId);

        missionCommandService.updateMissionNameIfPresent(mission, request);
    }

    @Caching(
            evict = {
                @CacheEvict(
                        cacheNames = MISSIONS,
                        key =
                                "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).missions(#memberId, #tripId, #stampId)"),
                @CacheEvict(
                        cacheNames = STAMP,
                        key =
                                "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).stamp(#memberId, #tripId, #stampId)")
            })
    @Transactional
    public void deleteMission(Long memberId, Long tripId, Long stampId, Long missionId) {
        Stamp stamp = getValidStampFromTripOwnedByMember(memberId, tripId, stampId);
        Mission mission = missionQueryService.getValidMission(stamp.getId(), missionId);

        missionCommandService.deleteMission(mission);
    }

    @Cacheable(
            cacheNames = MISSIONS,
            key =
                    "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).missions(#memberId, #tripId, #stampId)")
    @Transactional(readOnly = true)
    public List<MissionInfo> getMissionsByStamp(Long memberId, Long tripId, Long stampId) {
        Stamp stamp = getValidStampFromTripOwnedByMember(memberId, tripId, stampId);
        List<Mission> missions = missionQueryService.getMissionsByStampId(stamp.getId());

        return missions.stream().map(MissionInfo::from).toList();
    }

    private Stamp getValidStampFromTripOwnedByMember(Long memberId, Long tripId, Long stampId) {
        Trip trip = tripQueryService.getValidTrip(memberId, tripId);

        return stampQueryService.getValidStamp(trip.getId(), stampId);
    }
}
