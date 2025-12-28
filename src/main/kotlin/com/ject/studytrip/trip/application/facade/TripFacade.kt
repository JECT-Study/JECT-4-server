package com.ject.studytrip.trip.application.facade

import com.ject.studytrip.global.common.constants.CacheNameConstants.STAMP
import com.ject.studytrip.global.common.constants.CacheNameConstants.STAMPS
import com.ject.studytrip.global.common.constants.CacheNameConstants.TRIP
import com.ject.studytrip.global.common.constants.CacheNameConstants.TRIPS
import com.ject.studytrip.member.application.service.MemberQueryService
import com.ject.studytrip.stamp.application.dto.StampInfo
import com.ject.studytrip.stamp.application.service.StampCommandService
import com.ject.studytrip.stamp.application.service.StampQueryService
import com.ject.studytrip.trip.application.dto.TripCategoryInfo
import com.ject.studytrip.trip.application.dto.TripDetail
import com.ject.studytrip.trip.application.dto.TripInfo
import com.ject.studytrip.trip.application.dto.TripSliceInfo
import com.ject.studytrip.trip.application.service.TripCommandService
import com.ject.studytrip.trip.application.service.TripQueryService
import com.ject.studytrip.trip.domain.model.TripCategory
import com.ject.studytrip.trip.presentation.dto.request.CreateTripRequest
import com.ject.studytrip.trip.presentation.dto.request.UpdateTripRequest
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Component
class TripFacade(
    // Query Service
    private val memberQueryService: MemberQueryService,
    private val tripQueryService: TripQueryService,
    private val stampQueryService: StampQueryService,
    // Command Service
    private val tripCommandService: TripCommandService,
    private val stampCommandService: StampCommandService,
) {
    @CacheEvict(cacheNames = [TRIPS], allEntries = true)
    @Transactional
    fun createTrip(
        memberId: Long,
        request: CreateTripRequest,
    ): TripInfo {
        val member = memberQueryService.getValidMember(memberId)
        val trip = tripCommandService.createTrip(member, request)

        val nextOrder = stampQueryService.getNextStampOrderByTrip(trip)
        stampCommandService.createStamps(trip, nextOrder, request.stamps)

        return TripInfo.from(trip, null, null)
    }

    @Caching(
        evict = [
            CacheEvict(cacheNames = [TRIPS], allEntries = true),
            CacheEvict(
                cacheNames = [TRIP],
                key = "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).trip(#memberId, #tripId)",
            ),
            CacheEvict(
                cacheNames = [STAMPS],
                key = "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).stamps(#memberId, #tripId)",
            ),
            CacheEvict(cacheNames = [STAMP], allEntries = true),
        ],
    )
    @Transactional
    fun updateTrip(
        memberId: Long,
        tripId: Long,
        request: UpdateTripRequest,
    ) {
        val trip = tripQueryService.getValidTrip(memberId, tripId)

        tripCommandService.updateTrip(trip, request)

        if (request.category != null) {
            stampCommandService.updateStampOrdersByTripCategoryChange(trip.id, TripCategory.from(request.category))
        }
    }

    @Caching(
        evict = [
            CacheEvict(cacheNames = [TRIPS], allEntries = true),
            CacheEvict(
                cacheNames = [TRIP],
                key = "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).trip(#memberId, #tripId)",
            ),
            CacheEvict(
                cacheNames = [STAMPS],
                key = "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).stamps(#memberId, #tripId)",
            ),
        ],
    )
    @Transactional
    fun deleteTrip(
        memberId: Long,
        tripId: Long,
    ) {
        val trip = tripQueryService.getValidTrip(memberId, tripId)

        tripCommandService.deleteTrip(trip)
    }

    @Caching(
        evict = [
            CacheEvict(cacheNames = [TRIPS], allEntries = true),
            CacheEvict(
                cacheNames = [TRIP],
                key = "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).trip(#memberId, #tripId)",
            ),
            CacheEvict(
                cacheNames = [STAMPS],
                key = "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).stamps(#memberId, #tripId)",
            ),
        ],
    )
    @Transactional
    fun completeTrip(
        memberId: Long,
        tripId: Long,
    ) {
        val trip = tripQueryService.getValidTrip(memberId, tripId)

        stampCommandService.validateAllStampsCompletedByTripId(trip.id)

        tripCommandService.completeTrip(trip)
    }

    @Transactional(readOnly = true)
    fun loadTripCategories(): List<TripCategoryInfo> = TripCategory.entries.map(TripCategoryInfo::from)

    @Cacheable(
        cacheNames = [TRIPS],
        key = "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).trips(#memberId, #page, #size)",
    )
    @Transactional(readOnly = true)
    fun getTripsByMember(
        memberId: Long,
        page: Int,
        size: Int,
    ): TripSliceInfo {
        val tripSlice = tripQueryService.getTripsSliceByMemberId(memberId, page, size)

        val tripInfos: List<TripInfo> =
            tripSlice.content
                .map { trip ->
                    val dDay = calculateDDay(trip.endDate)
                    val progress = calculateProgress(trip.totalStamps, trip.completedStamps)
                    TripInfo.from(trip, dDay, progress)
                }.sortedWith(compareBy(nullsLast()) { it.dDay })

        return TripSliceInfo.of(tripInfos, tripSlice.hasNext())
    }

    @Cacheable(
        cacheNames = [TRIP],
        key = "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).trip(#memberId, #tripId)",
    )
    @Transactional(readOnly = true)
    fun getTrip(
        memberId: Long,
        tripId: Long,
    ): TripDetail {
        val member = memberQueryService.getValidMember(memberId)
        val trip = tripQueryService.getValidTrip(member.id, tripId)

        val dDay = calculateDDay(trip.endDate)
        val progress: Int = calculateProgress(trip.totalStamps, trip.completedStamps)

        val stamps = stampQueryService.getStampsByTripId(trip.id)
        val stampInfos = stamps.map { StampInfo.from(it) }

        return TripDetail.from(TripInfo.from(trip, dDay, progress), stampInfos)
    }

    private fun calculateDDay(endDate: LocalDate?): Int? {
        if (endDate == null) return null // 무기한 여행

        val today = LocalDate.now()

        return ChronoUnit.DAYS.between(today, endDate).toInt()
    }

    private fun calculateProgress(
        totalStamps: Int,
        completedStamps: Int,
    ): Int {
        if (totalStamps == 0) return 0

        val ratio = completedStamps.toDouble() / totalStamps.toDouble()

        return (ratio * 100).toInt()
    }
}
