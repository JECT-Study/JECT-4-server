package com.ject.studytrip.studylog.application.facade

import com.ject.studytrip.global.common.constants.CacheNameConstants.MISSIONS
import com.ject.studytrip.global.common.constants.CacheNameConstants.STAMP
import com.ject.studytrip.global.common.constants.CacheNameConstants.STAMPS
import com.ject.studytrip.global.common.constants.CacheNameConstants.STUDY_LOGS
import com.ject.studytrip.image.application.service.ImageService
import com.ject.studytrip.mission.application.service.DailyMissionQueryService
import com.ject.studytrip.mission.application.service.MissionCommandService
import com.ject.studytrip.mission.domain.model.DailyMission
import com.ject.studytrip.pomodoro.application.service.PomodoroCommandService
import com.ject.studytrip.pomodoro.application.service.PomodoroQueryService
import com.ject.studytrip.stamp.application.service.StampCommandService
import com.ject.studytrip.stamp.domain.model.Stamp
import com.ject.studytrip.studylog.application.dto.PresignedStudyLogImageInfo
import com.ject.studytrip.studylog.application.dto.StudyLogDetail
import com.ject.studytrip.studylog.application.dto.StudyLogInfo
import com.ject.studytrip.studylog.application.dto.StudyLogSliceInfo
import com.ject.studytrip.studylog.application.service.StudyLogCommandService
import com.ject.studytrip.studylog.application.service.StudyLogDailyMissionCommandService
import com.ject.studytrip.studylog.application.service.StudyLogDailyMissionQueryService
import com.ject.studytrip.studylog.application.service.StudyLogQueryService
import com.ject.studytrip.studylog.domain.model.StudyLog
import com.ject.studytrip.studylog.presentation.dto.request.ConfirmStudyLogImageRequest
import com.ject.studytrip.studylog.presentation.dto.request.CreateStudyLogRequest
import com.ject.studytrip.studylog.presentation.dto.request.PresignStudyLogImageRequest
import com.ject.studytrip.trip.application.service.DailyGoalQueryService
import com.ject.studytrip.trip.application.service.TripQueryService
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class StudyLogFacade(
    // Query Service
    private val tripQueryService: TripQueryService,
    private val studyLogQueryService: StudyLogQueryService,
    private val dailyMissionQueryService: DailyMissionQueryService,
    private val studyLogDailyMissionQueryService: StudyLogDailyMissionQueryService,
    private val dailyGoalQueryService: DailyGoalQueryService,
    private val pomodoroQueryService: PomodoroQueryService,
    // Command Service
    private val stampCommandService: StampCommandService,
    private val missionCommandService: MissionCommandService,
    private val studyLogCommandService: StudyLogCommandService,
    private val studyLogDailyMissionCommandService: StudyLogDailyMissionCommandService,
    private val pomodoroCommandService: PomodoroCommandService,
    // Image Service
    private val imageService: ImageService,
) {
    companion object {
        private const val STUDY_LOG_IMAGE_KEY_PREFIX: String = "study-logs"
    }

    @Caching(
        evict = [
            CacheEvict(cacheNames = [STUDY_LOGS], allEntries = true),
            CacheEvict(cacheNames = [MISSIONS], allEntries = true),
            CacheEvict(cacheNames = [STAMP], allEntries = true),
            CacheEvict(
                cacheNames = [STAMPS],
                key = "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).stamps(#memberId, #tripId)",
            ),
        ],
    )
    @Transactional
    fun createStudyLog(
        memberId: Long,
        tripId: Long,
        dailyGoalId: Long,
        request: CreateStudyLogRequest,
    ): StudyLogInfo {
        // 1. 유효성 검증 및 엔티티 조회
        val trip = tripQueryService.getValidTrip(memberId, tripId)
        val dailyGoal = dailyGoalQueryService.getValidDailyGoal(trip.id, dailyGoalId)
        val selectedDailyMissions =
            dailyMissionQueryService.getValidDailyMissionsWithMissionAndStampByIds(
                dailyGoal.id,
                request.selectedDailyMissionIds,
            )
        val pomodoro = pomodoroQueryService.getValidPomodoroByDailyGoalId(dailyGoal.id)

        // 2. 학습 로그 생성
        val studyLog = studyLogCommandService.createStudyLog(trip.member, dailyGoal, request.content)

        // 3. 뽀모도로 총 학습시간 업데이트
        pomodoroCommandService.updateTotalFocusTime(pomodoro, request.totalFocusTimeInSeconds)

        // 4. 연관 데이터 생성 및 미션 완료 처리
        createStudyLogDailyMissionsAndCompleteMissions(studyLog, selectedDailyMissions)

        return StudyLogInfo.from(studyLog)
    }

    @Cacheable(
        cacheNames = [STUDY_LOGS],
        key = "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).studyLogs(#memberId, #tripId, #page, #size, #order)",
    )
    @Transactional(readOnly = true)
    fun getStudyLogsByTrip(
        memberId: Long,
        tripId: Long,
        page: Int,
        size: Int,
        order: String,
    ): StudyLogSliceInfo {
        // 1. 유효성 검증 및 엔티티 조회
        val trip = tripQueryService.getValidTrip(memberId, tripId)

        // 2. 페이징된 학습 로그 목록 조회
        val studyLogSlice = studyLogQueryService.getStudyLogsSliceByTripId(trip.id, page, size, order)

        // 3. 학습 로그 상세 정보 구성
        return buildStudyLogDetailsSlice(studyLogSlice)
    }

    @Transactional(readOnly = true)
    fun issuePresignedUrl(
        studyLogId: Long,
        request: PresignStudyLogImageRequest,
    ): PresignedStudyLogImageInfo {
        val studyLog = studyLogQueryService.getValidStudyLog(studyLogId)
        val info = imageService.presign(STUDY_LOG_IMAGE_KEY_PREFIX, studyLog.id.toString(), request.originFilename)

        return PresignedStudyLogImageInfo.of(studyLog.id, info.tmpKey, info.presignedUrl)
    }

    @CacheEvict(cacheNames = [STUDY_LOGS], allEntries = true)
    @Transactional
    fun confirmImage(
        studyLogId: Long,
        request: ConfirmStudyLogImageRequest,
    ) {
        val studyLog = studyLogQueryService.getValidStudyLog(studyLogId)
        val imageUrl = imageService.confirm(request.tmpKey)

        studyLogCommandService.updateImageUrl(studyLog, imageUrl)
    }

    private fun createStudyLogDailyMissionsAndCompleteMissions(
        studyLog: StudyLog,
        selectedDailyMissions: List<DailyMission>,
    ) {
        // 학습 로그 데일리 미션 저장
        studyLogDailyMissionCommandService.createStudyLogDailyMissions(studyLog, selectedDailyMissions)
        val missions = selectedDailyMissions.map { it.mission }

        // 스탬프 ID를 기준으로 Stamp 집계
        val stampById = mutableMapOf<Long, Stamp>()

        // 스탬프 ID를 기준으로 완료된 미션 수 집계
        val completeMissionCountByStampId = mutableMapOf<Long, Int>()

        missions.forEach { mission ->
            val stamp = mission.stamp
            stampById.putIfAbsent(stamp.id, stamp)
            missionCommandService.completeMission(mission) // 미션 완료 처리
            completeMissionCountByStampId.merge(stamp.id, 1) { a, b -> a + b } // 스탬프별 완료한 미션 개수 누적(없으면 1, 있으면 +1)
        }

        // 스탬프별 완료된 미션 수 증가
        completeMissionCountByStampId.forEach { (stampId, completeMissions) ->
            stampById[stampId]?.let { stamp ->
                stampCommandService.increaseCompletedMissions(stamp, completeMissions)
            }
        }

        // NOTE: 현재는 데이터/트래픽이 적어 단건씩 처리 + 증분 갱신 방법 사용
        //       동시성/규모가 커지면 벌크 완료 + 스탬프의 완료된 미션 수 재계산으로 리팩토링도 가능할 것 같음
    }

    private fun buildStudyLogDetailsSlice(studyLogSlice: Slice<StudyLog>): StudyLogSliceInfo {
        val studyLogIds = studyLogSlice.content.map { it.id }

        // 학습 로그별 학습 로그 데일리 미션 목록 그룹화
        val groupedStudyLogDailyMissions = studyLogDailyMissionQueryService.getGroupedStudyLogDailyMissionsByStudyLogIds(studyLogIds)
        val studyLogDetails = studyLogSlice.content.map { StudyLogDetail.from(it, groupedStudyLogDailyMissions[it.id]) }

        return StudyLogSliceInfo.of(studyLogDetails, studyLogSlice.hasNext())
    }
}
