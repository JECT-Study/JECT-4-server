package com.ject.studytrip.trip.application.facade

import com.ject.studytrip.global.common.constants.CacheNameConstants.DAILY_GOAL
import com.ject.studytrip.global.util.EntityExtensions.requireId
import com.ject.studytrip.mission.application.dto.DailyMissionInfo
import com.ject.studytrip.mission.application.service.DailyMissionCommandService
import com.ject.studytrip.mission.application.service.DailyMissionQueryService
import com.ject.studytrip.mission.application.service.MissionCommandService
import com.ject.studytrip.mission.application.service.MissionQueryService
import com.ject.studytrip.mission.domain.model.Mission
import com.ject.studytrip.pomodoro.application.dto.PomodoroInfo
import com.ject.studytrip.pomodoro.application.service.PomodoroCommandService
import com.ject.studytrip.pomodoro.application.service.PomodoroQueryService
import com.ject.studytrip.stamp.application.service.StampCommandService
import com.ject.studytrip.stamp.application.service.StampQueryService
import com.ject.studytrip.trip.application.dto.DailyGoalDetail
import com.ject.studytrip.trip.application.dto.DailyGoalInfo
import com.ject.studytrip.trip.application.service.DailyGoalCommandService
import com.ject.studytrip.trip.application.service.DailyGoalQueryService
import com.ject.studytrip.trip.application.service.TripQueryService
import com.ject.studytrip.trip.domain.model.Trip
import com.ject.studytrip.trip.domain.model.TripCategory
import com.ject.studytrip.trip.presentation.dto.request.CreateDailyGoalRequest
import com.ject.studytrip.trip.presentation.dto.request.UpdateDailyGoalRequest
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class DailyGoalFacade(
    // Query Service
    private val tripQueryService: TripQueryService,
    private val stampQueryService: StampQueryService,
    private val missionQueryService: MissionQueryService,
    private val dailyGoalQueryService: DailyGoalQueryService,
    private val dailyMissionQueryService: DailyMissionQueryService,
    private val pomodoroQueryService: PomodoroQueryService,
    // Command Service
    private val stampCommandService: StampCommandService,
    private val missionCommandService: MissionCommandService,
    private val dailyGoalCommandService: DailyGoalCommandService,
    private val dailyMissionCommandService: DailyMissionCommandService,
    private val pomodoroCommandService: PomodoroCommandService,
) {
    @Transactional
    fun createDailyGoal(
        memberId: Long,
        tripId: Long,
        request: CreateDailyGoalRequest,
    ): DailyGoalInfo {
        val trip = tripQueryService.getValidTrip(memberId, tripId)
        val missions = getValidMissionsByTripCategory(trip, request.missionIds)
        val title = stampQueryService.getStampNameByTripCategory(trip.category, missions.map { it.stamp })

        val dailyGoal = dailyGoalCommandService.createDailyGoal(trip, title)
        dailyMissionCommandService.createDailyMissions(dailyGoal, missions)
        pomodoroCommandService.createPomodoro(dailyGoal, request.pomodoro)

        return DailyGoalInfo.from(dailyGoal)
    }

    @CacheEvict(
        cacheNames = [DAILY_GOAL],
        key = "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).dailyGoal(#memberId, #tripId, #dailyGoalId)",
    )
    @Transactional
    fun updateDailyGoal(
        memberId: Long,
        tripId: Long,
        dailyGoalId: Long,
        request: UpdateDailyGoalRequest,
    ) {
        val trip = tripQueryService.getValidTrip(memberId, tripId)
        val dailyGoal = dailyGoalQueryService.getValidDailyGoal(tripId, dailyGoalId)

        // 삭제할 데일리 미션이 있을 경우
        if (request.deleteDailyMissionIds.isNotEmpty()) {
            val deleteDailyMissions = dailyMissionQueryService.getValidDailyMissionsByIds(dailyGoalId, request.deleteDailyMissionIds)
            deleteDailyMissions.forEach { dailyMissionCommandService.deleteDailyMission(it) }
        }

        // 추가할 미션이 있을 경우
        if (request.addMissionIds.isNotEmpty()) {
            val addMissions = getValidMissionsByTripCategory(trip, request.addMissionIds)
            dailyMissionCommandService.createDailyMissions(dailyGoal, addMissions)
        }
    }

    @CacheEvict(
        cacheNames = [DAILY_GOAL],
        key = "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).dailyGoal(#memberId, #tripId, #dailyGoalId)",
    )
    @Transactional
    fun deleteDailyGoal(
        memberId: Long,
        tripId: Long,
        dailyGoalId: Long,
    ) {
        val trip = tripQueryService.getValidTrip(memberId, tripId)
        val dailyGoal = dailyGoalQueryService.getValidDailyGoal(trip.id.requireId(), dailyGoalId)
        val pomodoro = pomodoroQueryService.getValidPomodoroByDailyGoalId(dailyGoalId)

        // 뽀모도로 삭제
        pomodoroCommandService.deletePomodoro(pomodoro)

        // 데일리 미션 삭제
        val dailyMissions = dailyMissionQueryService.getDailyMissionsByDailyGoalId(dailyGoalId)
        dailyMissions.forEach { dailyMissionCommandService.deleteDailyMission(it) }

        // 데일리 목표 삭제
        dailyGoalCommandService.deleteDailyGoal(dailyGoal)
    }

    @Cacheable(
        cacheNames = [DAILY_GOAL],
        key = "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).dailyGoal(#memberId, #tripId, #dailyGoalId)",
    )
    @Transactional(readOnly = true)
    fun getDailyGoal(
        memberId: Long,
        tripId: Long,
        dailyGoalId: Long,
    ): DailyGoalDetail {
        val trip = tripQueryService.getValidTrip(memberId, tripId)
        val dailyGoal = dailyGoalQueryService.getValidDailyGoal(trip.id.requireId(), dailyGoalId)
        val pomodoro = pomodoroQueryService.getValidPomodoroByDailyGoalId(dailyGoalId)
        val dailyMissions = dailyMissionQueryService.getDailyMissionsByDailyGoalId(dailyGoalId)

        return DailyGoalDetail(DailyGoalInfo.from(dailyGoal), PomodoroInfo.from(pomodoro), dailyMissions.map(DailyMissionInfo::from))
    }

    private fun getValidMissionsByTripCategory(
        trip: Trip,
        missionIds: List<Long>,
    ): List<Mission> {
        val tripId = trip.id.requireId()
        val missions = missionQueryService.getValidMissionsByIds(missionIds)
        missions.forEach { stampCommandService.validateStampBelongsToTrip(tripId, it.stamp) }

        if (trip.category == TripCategory.COURSE) {
            val stamp = stampQueryService.getFirstInProcessingStampsForCourseTrip(tripId)
            missionCommandService.validateMissionsBelongToStamp(stamp.id.requireId(), missions)
        }

        return missions
    }
}
