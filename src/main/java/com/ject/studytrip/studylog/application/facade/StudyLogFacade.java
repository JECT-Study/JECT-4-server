package com.ject.studytrip.studylog.application.facade;

import com.ject.studytrip.mission.application.service.DailyMissionService;
import com.ject.studytrip.mission.application.service.MissionService;
import com.ject.studytrip.mission.domain.model.DailyMission;
import com.ject.studytrip.pomodoro.application.service.PomodoroService;
import com.ject.studytrip.studylog.application.dto.StudyLogDetail;
import com.ject.studytrip.studylog.application.dto.StudyLogInfo;
import com.ject.studytrip.studylog.application.service.StudyLogDailyMissionService;
import com.ject.studytrip.studylog.application.service.StudyLogService;
import com.ject.studytrip.studylog.domain.model.StudyLog;
import com.ject.studytrip.studylog.domain.model.StudyLogDailyMission;
import com.ject.studytrip.studylog.presentation.dto.request.CreateStudyLogRequest;
import com.ject.studytrip.trip.application.service.DailyGoalService;
import com.ject.studytrip.trip.application.service.TripService;
import com.ject.studytrip.trip.domain.model.DailyGoal;
import com.ject.studytrip.trip.domain.model.Trip;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudyLogFacade {
    private final TripService tripService;
    private final MissionService missionService;
    private final DailyGoalService dailyGoalService;
    private final DailyMissionService dailyMissionService;
    private final PomodoroService pomodoroService;
    private final StudyLogService studyLogService;
    private final StudyLogDailyMissionService studyLogDailyMissionService;

    @Transactional
    public StudyLogInfo createStudyLog(
            Long memberId, Long tripId, Long dailyGoalId, CreateStudyLogRequest request) {
        // 1. 유효성 검증 및 엔티티 조회
        Trip trip = tripService.getValidTrip(memberId, tripId);
        DailyGoal dailyGoal = dailyGoalService.getValidDailyGoal(trip.getId(), dailyGoalId);
        List<DailyMission> selectedDailyMissions =
                dailyMissionService.getValidDailyMissionsByIds(
                        dailyGoal.getId(), request.selectedDailyMissionIds());

        // 2. 학습 로그 생성
        StudyLog studyLog =
                studyLogService.createStudyLog(trip.getMember(), dailyGoal, request.content());

        // 3. 뽀모도로 총 학습시간 업데이트
        pomodoroService.updateTotalFocusTime(dailyGoalId, request.totalFocusTimeInSeconds());

        // 4. 연관 데이터 생성 및 미션 완료 처리
        createStudyLogDailyMissionsAndCompleteMissions(studyLog, selectedDailyMissions);

        return StudyLogInfo.from(studyLog);
    }

    private void createStudyLogDailyMissionsAndCompleteMissions(
            StudyLog studyLog, List<DailyMission> selectedDailyMissions) {
        // 학습 로그 데일리 미션 저장
        studyLogDailyMissionService.createStudyLogDailyMissions(studyLog, selectedDailyMissions);

        // 미션 완료 처리
        selectedDailyMissions.forEach(
                dailyMission -> missionService.completeMission(dailyMission.getMission()));
    }

    @Transactional(readOnly = true)
    public Slice<StudyLogDetail> getStudyLogsByTrip(
            Long memberId, Long tripId, int page, int size) {
        // 1. 유효성 검증 및 엔티티 조회
        Trip trip = tripService.getValidTrip(memberId, tripId);

        // 2. 페이징된 학습 로그 목록 조회
        Slice<StudyLog> studyLogSlice =
                studyLogService.getStudyLogsSliceByTripId(trip.getId(), page, size);

        // 3. 학습 로그 상세 정보 구성
        return buildStudyLogDetailsSlice(studyLogSlice);
    }

    private Slice<StudyLogDetail> buildStudyLogDetailsSlice(Slice<StudyLog> studyLogSlice) {
        List<Long> studyLogIds = studyLogSlice.getContent().stream().map(StudyLog::getId).toList();

        // 학습 로그별 학습 로그 데일리 미션 목록 그룹화
        Map<Long, List<StudyLogDailyMission>> groupedStudyLogDailyMissions =
                studyLogDailyMissionService.getGroupedStudyLogDailyMissionsByStudyLogIds(
                        studyLogIds);

        List<StudyLogDetail> studyLogDetails =
                studyLogSlice.getContent().stream()
                        .map(
                                studyLog ->
                                        StudyLogDetail.from(
                                                studyLog,
                                                groupedStudyLogDailyMissions.get(studyLog.getId())))
                        .toList();

        return new SliceImpl<>(
                studyLogDetails, studyLogSlice.getPageable(), studyLogSlice.hasNext());
    }
}
