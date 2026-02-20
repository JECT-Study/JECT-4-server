package com.ject.studytrip.stamp.application.service

import com.ject.studytrip.global.util.EntityExtensions.requireId
import com.ject.studytrip.stamp.domain.factory.StampFactory
import com.ject.studytrip.stamp.domain.model.Stamp
import com.ject.studytrip.stamp.domain.policy.StampPolicy
import com.ject.studytrip.stamp.domain.repository.StampCommandRepository
import com.ject.studytrip.stamp.domain.repository.StampRepository
import com.ject.studytrip.stamp.presentation.dto.request.CreateStampRequest
import com.ject.studytrip.stamp.presentation.dto.request.UpdateStampOrderRequest
import com.ject.studytrip.stamp.presentation.dto.request.UpdateStampRequest
import com.ject.studytrip.trip.domain.model.Trip
import com.ject.studytrip.trip.domain.model.TripCategory
import org.springframework.stereotype.Service

@Service
class StampCommandService(
    private val stampRepository: StampRepository,
    private val stampCommandRepository: StampCommandRepository,
) {
    fun createStamp(
        trip: Trip,
        nextOrder: Int,
        request: CreateStampRequest,
    ): Stamp {
        val stamp = StampFactory.create(trip, request.name, nextOrder, request.endDate)

        StampPolicy.validateNotStampEndDateAfterTripEndDate(trip.endDate, stamp.endDate)

        return stampRepository.save(stamp)
    }

    fun createStamps(
        trip: Trip,
        nextOrder: Int,
        requests: List<CreateStampRequest>?,
    ) {
        if (requests.isNullOrEmpty()) return

        val stamps: List<Stamp> =
            when (trip.category) {
                // 탐험형 여행이라면 order 0 으로 전부 고정
                TripCategory.EXPLORE -> {
                    requests.map { request ->
                        StampFactory.create(trip, request.name, 0, request.endDate)
                    }
                }

                // 코스형 여행이라면 nextOrder 부터 1씩 증가
                TripCategory.COURSE -> {
                    requests.mapIndexed { index, request ->
                        StampFactory.create(trip, request.name, nextOrder + index, request.endDate)
                    }
                }
            }

        stamps.forEach { stamp ->
            StampPolicy.validateNotStampEndDateAfterTripEndDate(trip.endDate, stamp.endDate)
        }

        stampRepository.saveAll(stamps)
    }

    fun updateStamp(
        trip: Trip,
        stamp: Stamp,
        request: UpdateStampRequest,
    ) {
        request.name?.let {
            stamp.updateName(it)
        }

        request.endDate?.let {
            StampPolicy.validateNotStampEndDateAfterTripEndDate(trip.endDate, it)
            stamp.updateEndDate(it)
        }
    }

    fun updateStampOrders(
        trip: Trip,
        request: UpdateStampOrderRequest,
    ) {
        // 배치 조회 (ID 목록 기준으로 조회하지만 순서는 보장되지 않음)
        val stamps = stampRepository.findAllByIdIn(request.orderedStampIds)

        StampPolicy.validateUpdateStampOrders(trip.category, request.orderedStampIds, stamps)
        stamps.forEach { stamp ->
            StampPolicy.validateStampBelongsToTrip(trip.id.requireId(), stamp)
            StampPolicy.validateNotDeleted(stamp)
            StampPolicy.validateNotCompleted(stamp)
        }

        // 조회된 스탬프 ID 를 기준으로 매핑
        val stampMap = stamps.associateBy { it.id }

        // 요청에서 전달된 ID 순서를 기준으로 스탬프 리스트 재정렬
        val orderedStamps = request.orderedStampIds.mapNotNull { stampMap[it] }

        orderedStamps.forEachIndexed { index, stamp -> stamp.updateStampOrder(index + 1) }
    }

    fun updateStampOrdersByTripCategoryChange(
        tripId: Long,
        newCategory: TripCategory,
    ) {
        val stamps = stampRepository.findAllByTripIdOrderByCreatedAtAsc(tripId)

        when (newCategory) {
            // 탐험형
            TripCategory.EXPLORE -> stamps.forEach { stamp -> stamp.updateStampOrder(0) }

            // 코스형
            TripCategory.COURSE -> stamps.forEachIndexed { index, stamp -> stamp.updateStampOrder(index + 1) }
        }
    }

    fun deleteStamp(stamp: Stamp) = stamp.updateDeletedAt()

    fun completeStamp(stamp: Stamp) = stamp.updateCompleted()

    fun shiftStampOrders(affectedStamps: List<Stamp>) = affectedStamps.forEach { stamp -> stamp.updateStampOrder(stamp.stampOrder - 1) }

    fun increaseTotalMissions(stamp: Stamp) = stamp.increaseTotalMissions()

    fun decreaseTotalMissions(stamp: Stamp) = stamp.decreaseTotalMissions()

    fun increaseCompletedMissions(
        stamp: Stamp,
        count: Int,
    ) = stamp.increaseCompletedMissions(count)

    fun validateStampBelongsToTrip(
        tripId: Long,
        stamp: Stamp,
    ) = StampPolicy.validateStampBelongsToTrip(tripId, stamp)

    fun validateAllStampsCompletedByTripId(tripId: Long) {
        val exists = stampCommandRepository.existsByTripIdAndCompletedIsFalseAndDeletedAtIsNull(tripId)
        StampPolicy.validateNotAllCompleted(exists)
    }

    fun hardDeleteStamps() = stampCommandRepository.deleteAllByDeletedAtIsNotNull()

    fun hardDeleteStampsOwnedByDeletedTrip() = stampCommandRepository.deleteAllByDeletedTripOwner()

    fun hardDeleteStampsOwnedByMember(memberId: Long) = stampCommandRepository.deleteAllByMemberId(memberId)
}
