package com.ject.studytrip.mission.application.facade

import com.ject.studytrip.global.common.constants.CacheNameConstants.MISSIONS
import com.ject.studytrip.global.common.constants.CacheNameConstants.STAMP
import com.ject.studytrip.global.common.constants.CacheNameConstants.STAMPS
import com.ject.studytrip.global.common.constants.CacheNameConstants.TRIP
import com.ject.studytrip.global.common.constants.CacheNameConstants.TRIPS
import com.ject.studytrip.global.util.EntityExtensions.requireId
import com.ject.studytrip.mission.application.dto.MissionInfo
import com.ject.studytrip.mission.application.dto.MissionsInfo
import com.ject.studytrip.mission.application.service.MissionCommandService
import com.ject.studytrip.mission.application.service.MissionQueryService
import com.ject.studytrip.mission.presentation.dto.request.CreateMissionRequest
import com.ject.studytrip.mission.presentation.dto.request.UpdateMissionRequest
import com.ject.studytrip.stamp.application.service.StampCommandService
import com.ject.studytrip.stamp.application.service.StampQueryService
import com.ject.studytrip.stamp.domain.model.Stamp
import com.ject.studytrip.trip.application.service.TripQueryService
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class MissionFacade(
    // Query Service
    private val tripQueryService: TripQueryService,
    private val stampQueryService: StampQueryService,
    private val missionQueryService: MissionQueryService,
    // Command Service
    private val stampCommandService: StampCommandService,
    private val missionCommandService: MissionCommandService,
) {
    @Caching(
        evict = [
            CacheEvict(
                cacheNames = [MISSIONS],
                key = "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).missions(#memberId, #tripId, #stampId)",
            ),
            CacheEvict(
                cacheNames = [STAMP],
                key = "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).stamp(#memberId, #tripId, #stampId)",
            ),
            CacheEvict(
                cacheNames = [TRIP],
                key = "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).trip(#memberId, #tripId)",
            ),
            CacheEvict(cacheNames = [TRIPS], allEntries = true),
            CacheEvict(
                cacheNames = [STAMPS],
                key = "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).stamps(#memberId, #tripId)",
            ),
        ],
    )
    @Transactional
    fun createMission(
        memberId: Long,
        tripId: Long,
        stampId: Long,
        request: CreateMissionRequest,
    ): MissionInfo {
        val stamp = getValidStampForTripOwnedByMember(memberId, tripId, stampId)
        val mission = missionCommandService.createMission(stamp, request)

        stampCommandService.increaseTotalMissions(stamp)

        return MissionInfo.from(mission)
    }

    @Caching(
        evict = [
            CacheEvict(
                cacheNames = [MISSIONS],
                key = "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).missions(#memberId, #tripId, #stampId)",
            ),
            CacheEvict(
                cacheNames = [STAMP],
                key = "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).stamp(#memberId, #tripId, #stampId)",
            ),
            CacheEvict(
                cacheNames = [TRIP],
                key = "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).trip(#memberId, #tripId)",
            ),
            CacheEvict(cacheNames = [TRIPS], allEntries = true),
        ],
    )
    @Transactional
    fun updateMissionNameIfPresent(
        memberId: Long,
        tripId: Long,
        stampId: Long,
        missionId: Long,
        request: UpdateMissionRequest,
    ) {
        val stamp = getValidStampForTripOwnedByMember(memberId, tripId, stampId)
        val mission = missionQueryService.getValidMission(stamp.id.requireId(), missionId)

        missionCommandService.updateMissionNameIfPresent(mission, request)
    }

    @Caching(
        evict = [
            CacheEvict(
                cacheNames = [MISSIONS],
                key = "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).missions(#memberId, #tripId, #stampId)",
            ),
            CacheEvict(
                cacheNames = [STAMP],
                key = "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).stamp(#memberId, #tripId, #stampId)",
            ),
            CacheEvict(
                cacheNames = [TRIP],
                key = "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).trip(#memberId, #tripId)",
            ),
            CacheEvict(cacheNames = [TRIPS], allEntries = true),
        ],
    )
    @Transactional
    fun deleteMission(
        memberId: Long,
        tripId: Long,
        stampId: Long,
        missionId: Long,
    ) {
        val stamp = getValidStampForTripOwnedByMember(memberId, tripId, stampId)
        val mission = missionQueryService.getValidMission(stamp.id.requireId(), missionId)

        missionCommandService.deleteMission(mission)
        stampCommandService.decreaseTotalMissions(stamp)
    }

    @Cacheable(
        cacheNames = [MISSIONS],
        key = "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).missions(#memberId, #tripId, #stampId)",
    )
    @Transactional(readOnly = true)
    fun getMissionsByStamp(
        memberId: Long,
        tripId: Long,
        stampId: Long,
    ): MissionsInfo {
        val stamp = getValidStampForTripOwnedByMember(memberId, tripId, stampId)
        val missions = missionQueryService.getMissionsByStampId(stamp.id.requireId())

        return MissionsInfo(missions.map { MissionInfo.from(it) })
    }

    private fun getValidStampForTripOwnedByMember(
        memberId: Long,
        tripId: Long,
        stampId: Long,
    ): Stamp {
        val trip = tripQueryService.getValidTrip(memberId, tripId)

        return stampQueryService.getValidStamp(trip.id.requireId(), stampId)
    }
}
