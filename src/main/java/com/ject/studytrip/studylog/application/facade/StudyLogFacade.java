package com.ject.studytrip.studylog.application.facade;

import static com.ject.studytrip.global.common.constants.CacheNameConstants.MISSIONS;
import static com.ject.studytrip.global.common.constants.CacheNameConstants.STUDY_LOGS;

import com.ject.studytrip.image.application.dto.PresignedImageInfo;
import com.ject.studytrip.image.application.service.ImageService;
import com.ject.studytrip.mission.application.service.DailyMissionQueryService;
import com.ject.studytrip.mission.application.service.MissionCommandService;
import com.ject.studytrip.mission.domain.model.DailyMission;
import com.ject.studytrip.mission.domain.model.Mission;
import com.ject.studytrip.pomodoro.application.service.PomodoroCommandService;
import com.ject.studytrip.pomodoro.application.service.PomodoroQueryService;
import com.ject.studytrip.pomodoro.domain.model.Pomodoro;
import com.ject.studytrip.stamp.application.service.StampCommandService;
import com.ject.studytrip.stamp.domain.model.Stamp;
import com.ject.studytrip.studylog.application.dto.*;
import com.ject.studytrip.studylog.application.service.*;
import com.ject.studytrip.studylog.domain.model.StudyLog;
import com.ject.studytrip.studylog.domain.model.StudyLogDailyMission;
import com.ject.studytrip.studylog.presentation.dto.request.ConfirmStudyLogImageRequest;
import com.ject.studytrip.studylog.presentation.dto.request.CreateStudyLogRequest;
import com.ject.studytrip.studylog.presentation.dto.request.PresignStudyLogImageRequest;
import com.ject.studytrip.trip.application.service.DailyGoalQueryService;
import com.ject.studytrip.trip.application.service.TripQueryService;
import com.ject.studytrip.trip.domain.model.DailyGoal;
import com.ject.studytrip.trip.domain.model.Trip;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class StudyLogFacade {
    private static final String STUDY_LOG_IMAGE_KEY_PREFIX = "study-logs";

    private final TripQueryService tripQueryService;
    private final StudyLogQueryService studyLogQueryService;
    private final DailyMissionQueryService dailyMissionQueryService;
    private final StudyLogDailyMissionQueryService studyLogDailyMissionQueryService;
    private final DailyGoalQueryService dailyGoalQueryService;
    private final PomodoroQueryService pomodoroQueryService;

    private final StampCommandService stampCommandService;
    private final MissionCommandService missionCommandService;
    private final StudyLogCommandService studyLogCommandService;
    private final StudyLogDailyMissionCommandService studyLogDailyMissionCommandService;
    private final PomodoroCommandService pomodoroCommandService;

    private final ImageService imageService;

    @Caching(
            evict = {
                @CacheEvict(cacheNames = STUDY_LOGS, allEntries = true),
                @CacheEvict(cacheNames = MISSIONS, allEntries = true)
            })
    @Transactional
    public StudyLogInfo createStudyLog(
            Long memberId, Long tripId, Long dailyGoalId, CreateStudyLogRequest request) {
        // 1. 유효성 검증 및 엔티티 조회
        Trip trip = tripQueryService.getValidTrip(memberId, tripId);
        DailyGoal dailyGoal = dailyGoalQueryService.getValidDailyGoal(trip.getId(), dailyGoalId);
        List<DailyMission> selectedDailyMissions =
                dailyMissionQueryService.getValidDailyMissionsWithMissionAndStampByIds(
                        dailyGoal.getId(), request.selectedDailyMissionIds());
        Pomodoro pomodoro = pomodoroQueryService.getValidPomodoroByDailyGoal(dailyGoalId);

        // 2. 학습 로그 생성
        StudyLog studyLog =
                studyLogCommandService.createStudyLog(
                        trip.getMember(), dailyGoal, request.content());

        // 3. 뽀모도로 총 학습시간 업데이트
        pomodoroCommandService.updateTotalFocusTime(pomodoro, request.totalFocusTimeInSeconds());

        // 4. 연관 데이터 생성 및 미션 완료 처리
        createStudyLogDailyMissionsAndCompleteMissions(studyLog, selectedDailyMissions);

        return StudyLogInfo.from(studyLog);
    }

    @Cacheable(
            cacheNames = STUDY_LOGS,
            key =
                    "T(com.ject.studytrip.global.common.factory.CacheKeyFactory).studyLogs(#memberId, #tripId, #page, #size, #order)")
    @Transactional(readOnly = true)
    public StudyLogSliceInfo getStudyLogsByTrip(
            Long memberId, Long tripId, int page, int size, String order) {
        // 1. 유효성 검증 및 엔티티 조회
        Trip trip = tripQueryService.getValidTrip(memberId, tripId);

        // 2. 페이징된 학습 로그 목록 조회
        Slice<StudyLog> studyLogSlice =
                studyLogQueryService.getStudyLogsSliceByTripId(trip.getId(), page, size, order);

        // 3. 학습 로그 상세 정보 구성
        return buildStudyLogDetailsSlice(studyLogSlice);
    }

    @Transactional(readOnly = true)
    public PresignedStudyLogImageInfo issuePresignedUrl(
            Long studyLogId, PresignStudyLogImageRequest request) {
        StudyLog studyLog = studyLogQueryService.getValidStudyLog(studyLogId);
        PresignedImageInfo info =
                imageService.presign(
                        STUDY_LOG_IMAGE_KEY_PREFIX,
                        studyLog.getId().toString(),
                        request.originFilename());

        return PresignedStudyLogImageInfo.of(studyLog.getId(), info.tmpKey(), info.presignedUrl());
    }

    @Transactional
    public void confirmImage(Long studyLogId, ConfirmStudyLogImageRequest request) {
        StudyLog studyLog = studyLogQueryService.getValidStudyLog(studyLogId);
        String imageUrl = imageService.confirm(request.tmpKey());

        studyLogCommandService.updateImageUrl(studyLog, imageUrl);
    }

    private void createStudyLogDailyMissionsAndCompleteMissions(
            StudyLog studyLog, List<DailyMission> selectedDailyMissions) {
        // 학습 로그 데일리 미션 저장
        studyLogDailyMissionCommandService.createStudyLogDailyMissions(
                studyLog, selectedDailyMissions);

        List<Mission> missions =
                selectedDailyMissions.stream().map(DailyMission::getMission).toList();

        // 스탬프 ID를 기준으로 Stamp 집계
        Map<Long, Stamp> stampById = new HashMap<>();

        // 스탬프 ID를 기준으로 완료된 미션 수 집계
        Map<Long, Integer> completeMissionCountByStampId = new HashMap<>();

        missions.forEach(
                mission -> {
                    Stamp stamp = mission.getStamp();
                    stampById.putIfAbsent(stamp.getId(), stamp);

                    // 미션 완료 처리
                    missionCommandService.completeMission(mission);

                    // 스탬프별 완료한 미션 개수 누적(없으면 1, 있으면 +1)
                    completeMissionCountByStampId.merge(stamp.getId(), 1, Integer::sum);
                });

        // 스탬프별 완료된 미션 수 증가
        completeMissionCountByStampId.forEach(
                (stampId, completeMissions) -> {
                    Stamp stamp = stampById.get(stampId);
                    stampCommandService.increaseCompletedMissions(stamp, completeMissions);
                });

        // NOTE: 현재는 데이터/트래픽이 적어 단건씩 처리 + 증분 갱신 방법 사용
        //       동시성/규모가 커지면 벌크 완료 + 스탬프의 완료된 미션 수 재계산으로 리팩토링도 가능할 것 같음
    }

    private StudyLogSliceInfo buildStudyLogDetailsSlice(Slice<StudyLog> studyLogSlice) {
        List<Long> studyLogIds = studyLogSlice.getContent().stream().map(StudyLog::getId).toList();

        // 학습 로그별 학습 로그 데일리 미션 목록 그룹화
        Map<Long, List<StudyLogDailyMission>> groupedStudyLogDailyMissions =
                studyLogDailyMissionQueryService.getGroupedStudyLogDailyMissionsByStudyLogIds(
                        studyLogIds);

        List<StudyLogDetail> studyLogDetails =
                studyLogSlice.getContent().stream()
                        .map(
                                studyLog ->
                                        StudyLogDetail.from(
                                                studyLog,
                                                groupedStudyLogDailyMissions.get(studyLog.getId())))
                        .toList();

        return StudyLogSliceInfo.of(studyLogDetails, studyLogSlice.hasNext());
    }
}
