package com.ject.studytrip.stamp.application.facade

import com.ject.studytrip.global.common.constants.CacheNameConstants.STAMP
import com.ject.studytrip.global.common.constants.CacheNameConstants.STAMPS
import com.ject.studytrip.global.common.constants.CacheNameConstants.TRIP
import com.ject.studytrip.global.common.constants.CacheNameConstants.TRIPS
import com.ject.studytrip.global.util.EntityExtensions.requireId
import com.ject.studytrip.mission.application.dto.MissionInfo
import com.ject.studytrip.mission.application.service.MissionCommandService
import com.ject.studytrip.mission.application.service.MissionQueryService
import com.ject.studytrip.stamp.application.dto.StampDetail
import com.ject.studytrip.stamp.application.dto.StampInfo
import com.ject.studytrip.stamp.application.dto.StampsInfo
import com.ject.studytrip.stamp.application.service.StampCommandService
import com.ject.studytrip.stamp.application.service.StampQueryService
import com.ject.studytrip.stamp.presentation.dto.request.CreateStampRequest
import com.ject.studytrip.stamp.presentation.dto.request.UpdateStampOrderRequest
import com.ject.studytrip.stamp.presentation.dto.request.UpdateStampRequest
import com.ject.studytrip.trip.application.service.TripCommandService
import com.ject.studytrip.trip.application.service.TripQueryService
import com.ject.studytrip.trip.domain.model.Trip
import com.ject.studytrip.trip.domain.model.TripCategory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class StampFacade(
    // Query Service
    private val tripQueryService: TripQueryService,
    private val stampQueryService: StampQueryService,
    private val missionQueryService: MissionQueryService,
    // Command Service
    private val tripCommandService: TripCommandService,
    private val stampCommandService: StampCommandService,
    private val missionCommandService: MissionCommandService,
) {
    @Caching(
        evict = [
            CacheEvict(
                cacheNames = [STAMPS],
                key = "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).stamps(#memberId, #tripId)",
            ),
            CacheEvict(
                cacheNames = [TRIP],
                key = "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).trip(#memberId, #tripId)",
            ),
            CacheEvict(cacheNames = [TRIPS], allEntries = true),
        ],
    )
    @Transactional
    fun createStamp(
        memberId: Long,
        tripId: Long,
        request: CreateStampRequest,
    ): StampInfo {
        val trip = tripQueryService.getValidTrip(memberId, tripId)
        val nextOrder = stampQueryService.getNextStampOrderByTrip(trip)
        val stamp = stampCommandService.createStamp(trip, nextOrder, request)

        tripCommandService.increaseTotalStamps(trip)

        return StampInfo.from(stamp)
    }

    @Caching(
        evict = [
            CacheEvict(
                cacheNames = [STAMPS],
                key = "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).stamps(#memberId, #tripId)",
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
    fun updateStamp(
        memberId: Long,
        tripId: Long,
        stampId: Long,
        request: UpdateStampRequest,
    ) {
        val trip = tripQueryService.getValidTrip(memberId, tripId)
        val stamp = stampQueryService.getValidStamp(trip.id.requireId(), stampId)

        stampCommandService.updateStamp(trip, stamp, request)
    }

    @Caching(
        evict = [
            CacheEvict(
                cacheNames = [STAMPS],
                key = "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).stamps(#memberId, #tripId)",
            ),
            CacheEvict(cacheNames = [STAMP], allEntries = true),
            CacheEvict(
                cacheNames = [TRIP],
                key = "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).trip(#memberId, #tripId)",
            ),
            CacheEvict(cacheNames = [TRIPS], allEntries = true),
        ],
    )
    @Transactional
    fun updateStampOrders(
        memberId: Long,
        tripId: Long,
        request: UpdateStampOrderRequest,
    ) {
        val trip = tripQueryService.getValidTrip(memberId, tripId)

        stampCommandService.updateStampOrders(trip, request)
    }

    @Caching(
        evict = [
            CacheEvict(
                cacheNames = [STAMPS],
                key = "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).stamps(#memberId, #tripId)",
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
    fun deleteStamp(
        memberId: Long,
        tripId: Long,
        stampId: Long,
    ) {
        val trip = tripQueryService.getValidTrip(memberId, tripId)
        val stamp = stampQueryService.getValidStamp(tripId, stampId)

        stampCommandService.deleteStamp(stamp)
        shiftStampOrdersIfTripCategoryIsCourse(trip, stamp.stampOrder)
        tripCommandService.decreaseTotalStamps(trip)
    }

    @Caching(
        evict = [
            CacheEvict(
                cacheNames = [STAMPS],
                key = "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).stamps(#memberId, #tripId)",
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
    fun completeStamp(
        memberId: Long,
        tripId: Long,
        stampId: Long,
    ) {
        val trip = tripQueryService.getValidTrip(memberId, tripId)
        val stamp = stampQueryService.getValidStamp(tripId, stampId)

        missionCommandService.validateAllMissionsCompletedByStampId(stampId)

        stampCommandService.completeStamp(stamp)
        tripCommandService.increaseCompletedStamps(trip)
    }

    @Cacheable(
        cacheNames = [STAMPS],
        key = "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).stamps(#memberId, #tripId)",
    )
    @Transactional(readOnly = true)
    fun getStampsByTrip(
        memberId: Long,
        tripId: Long,
    ): StampsInfo {
        val trip = tripQueryService.getValidTrip(memberId, tripId)
        val stamps = stampQueryService.getStampsByTripId(trip.id.requireId())

        return StampsInfo(stamps.map { StampInfo.from(it) })
    }

    @Cacheable(
        cacheNames = [STAMP],
        key = "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).stamp(#memberId, #tripId, #stampId)",
    )
    @Transactional(readOnly = true)
    fun getStamp(
        memberId: Long,
        tripId: Long,
        stampId: Long,
    ): StampDetail {
        val trip = tripQueryService.getValidTrip(memberId, tripId)
        val stamp = stampQueryService.getValidStamp(trip.id.requireId(), stampId)
        val missions = missionQueryService.getMissionsByStampId(stampId)

        return StampDetail(StampInfo.from(stamp), missions.map { MissionInfo.from(it) })
    }

    private fun shiftStampOrdersIfTripCategoryIsCourse(
        trip: Trip,
        stampOrder: Int,
    ) {
        if (trip.category != TripCategory.COURSE) return

        val affectedStamps = stampQueryService.getStampsToShiftAfterDeleted(trip.id.requireId(), stampOrder)

        stampCommandService.shiftStampOrders(affectedStamps)
    }
}
