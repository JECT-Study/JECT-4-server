package com.ject.studytrip.stamp.application.service

import com.ject.studytrip.global.exception.CustomException
import com.ject.studytrip.global.util.EntityExtensions.requireId
import com.ject.studytrip.stamp.domain.error.StampErrorCode
import com.ject.studytrip.stamp.domain.model.Stamp
import com.ject.studytrip.stamp.domain.policy.StampPolicy
import com.ject.studytrip.stamp.domain.repository.StampQueryRepository
import com.ject.studytrip.stamp.domain.repository.StampRepository
import com.ject.studytrip.trip.domain.model.Trip
import com.ject.studytrip.trip.domain.model.TripCategory
import org.springframework.stereotype.Service

@Service
class StampQueryService(
    private val stampRepository: StampRepository,
    private val stampQueryRepository: StampQueryRepository,
) {
    fun getValidStamp(
        tripId: Long,
        stampId: Long,
    ): Stamp {
        val stamp =
            stampRepository
                .findById(stampId)
                .orElseThrow { CustomException(StampErrorCode.STAMP_NOT_FOUND) }

        StampPolicy.validateStampBelongsToTrip(tripId, stamp)
        StampPolicy.validateNotDeleted(stamp)
        StampPolicy.validateNotCompleted(stamp)

        return stamp
    }

    fun getStampsByTripId(tripId: Long): List<Stamp> = stampRepository.findAllByTripIdAndDeletedAtIsNull(tripId)

    fun getFirstInProcessingStampsForCourseTrip(tripId: Long): Stamp =
        stampQueryRepository
            .findFirstIncompleteStampByTripId(tripId)
            .orElseThrow { CustomException(StampErrorCode.STAMP_NOT_FOUND) }

    fun getStampNameByTripCategory(
        tripCategory: TripCategory,
        stamps: List<Stamp>,
    ): String {
        // 스탬프 목록이 비어있지 않은지 검증
        StampPolicy.validateNotStampListEmpty(stamps)
        stamps.forEach { stamp ->
            StampPolicy.validateNotDeleted(stamp)
            StampPolicy.validateNotCompleted(stamp)
        }

        // 코스형 여행은 상위 검증에서 동일한 스탬프인지 검증이 완료된 상태이므로, 스탬프 리스트에서 첫번째 스탬프 이름을 추출해도 안전
        if (tripCategory == TripCategory.COURSE) return stamps.first().name

        return getExploreStampName(stamps)
    }

    fun getNextStampOrderByTrip(trip: Trip): Int {
        if (trip.category != TripCategory.COURSE) return 0

        return stampQueryRepository.findNextStampOrderByTripId(trip.id.requireId())
    }

    fun getStampsToShiftAfterDeleted(
        tripId: Long,
        deletedStampOrder: Int,
    ): List<Stamp> = stampQueryRepository.findStampsToShiftAfterOrder(tripId, deletedStampOrder)

    private fun getExploreStampName(stamps: List<Stamp>): String {
        // 스탬프별 개수 집계
        val stampCountMap: Map<Stamp, Long> =
            stamps
                .groupingBy { it }
                .eachCount()
                .mapValues { it.value.toLong() }

        // 최대 개수
        val maxCount: Long = stampCountMap.values.maxOrNull() ?: 0L

        // 최대 개수를 가진 스탬프들 찾기
        val maxCountStamps: List<Stamp> =
            stampCountMap
                .filterValues { it == maxCount }
                .keys
                .toList()

        // 가장 빠른 생성 시간을 가진 스탬프 선택
        return maxCountStamps
            .minByOrNull { it.createdAt!! }
            ?.name
            ?: ""
    }
}
