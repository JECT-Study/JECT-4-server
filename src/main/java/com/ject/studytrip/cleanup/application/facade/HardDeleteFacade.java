package com.ject.studytrip.cleanup.application.facade;

import com.ject.studytrip.cleanup.application.executor.HardDeleteExecutor;
import com.ject.studytrip.member.application.service.MemberCommandService;
import com.ject.studytrip.mission.application.service.DailyMissionCommandService;
import com.ject.studytrip.mission.application.service.MissionCommandService;
import com.ject.studytrip.pomodoro.application.service.PomodoroCommandService;
import com.ject.studytrip.stamp.application.service.StampCommandService;
import com.ject.studytrip.studylog.application.service.StudyLogCommandService;
import com.ject.studytrip.studylog.application.service.StudyLogDailyMissionCommandService;
import com.ject.studytrip.trip.application.service.DailyGoalCommandService;
import com.ject.studytrip.trip.application.service.TripCommandService;
import com.ject.studytrip.trip.application.service.TripReportCommandService;
import com.ject.studytrip.trip.application.service.TripReportStudyLogCommandService;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class HardDeleteFacade {
    private final MemberCommandService memberCommandService;
    private final TripCommandService tripCommandService;
    private final StampCommandService stampCommandService;
    private final MissionCommandService missionCommandService;
    private final StudyLogCommandService studyLogCommandService;
    private final DailyMissionCommandService dailyMissionCommandService;
    private final StudyLogDailyMissionCommandService studyLogDailyMissionCommandService;
    private final DailyGoalCommandService dailyGoalCommandService;
    private final PomodoroCommandService pomodoroCommandService;
    private final TripReportCommandService tripReportCommandService;
    private final TripReportStudyLogCommandService tripReportStudyLogCommandService;

    private final HardDeleteExecutor executor;

    private static final String POMODOROS_OWNED_BY_DELETED_DAILY_GOAL =
            "pomodorosOwnedByDeletedDailyGoal";
    private static final String STUDY_LOG_DAILY_MISSIONS_OWNED_BY_DELETED_DAILY_MISSION =
            "studyLogDailyMissionsOwnedByDeletedDailyMission";
    private static final String STUDY_LOG_DAILY_MISSIONS_OWNED_BY_DELETED_STUDY_LOG =
            "studyLogDailyMissionsOwnedByDeletedStudyLog";
    private static final String STUDY_LOGS_OWNED_BY_DELETED_MEMBER =
            "studyLogsOwnedByDeletedMember";
    private static final String STUDY_LOGS_OWNED_BY_DELETED_DAILY_GOAL =
            "studyLogsOwnedByDeletedDailyGoal";
    private static final String DAILY_MISSIONS_OWNED_BY_DELETED_MISSION =
            "dailyMissionsOwnedByDeletedMission";
    private static final String DAILY_MISSIONS_OWNED_BY_DELETED_DAILY_GOAL =
            "dailyMissionsOwnedByDeletedDailyGoal";
    private static final String TRIP_REPORT_STUDY_LOGS_OWNED_BY_DELETED_MEMBER =
            "tripReportStudyLogsOwnedByDeletedMember";
    private static final String TRIP_REPORTS_OWNED_BY_DELETED_MEMBER =
            "tripReportsOwnedByDeletedMember";

    private static final String POMODOROS = "pomodoros";
    private static final String STUDY_LOG_DAILY_MISSIONS = "studyLogDailyMissions";
    private static final String STUDY_LOGS = "studyLogs";
    private static final String DAILY_MISSIONS = "dailyMissions";
    private static final String MISSIONS_OWNED_BY_DELETED_STAMP = "missionsOwnedByDeletedStamp";
    private static final String MISSIONS = "missions";
    private static final String STAMPS_OWNED_BY_DELETED_TRIP = "stampsOwnedByDeletedTrip";
    private static final String STAMPS = "stamps";
    private static final String TRIPS_OWNED_BY_DELETED_MEMBER = "tripsOwnedByDeletedMember";
    private static final String TRIPS = "trips";
    private static final String DAILY_GOALS_OWNED_BY_DELETED_TRIP = "dailyGoalsOwnedByDeletedTrip";
    private static final String DAILY_GOALS = "dailyGoals";
    private static final String TRIP_REPORTS = "tripReports";
    private static final String MEMBERS = "members";

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void hardDeleteAll() {
        Map<String, Long> phases = new LinkedHashMap<>();

        deletePomodoros(phases); // 뽀모도로 삭제
        deleteStudyLogDailyMissions(phases); // StudyLogDailyMission 삭제
        deleteDailyMissions(phases); // 데일리 미션 삭제
        deleteTripReportStudyLogs(phases); // TripReportStudyLog 삭제
        deleteTripReports(phases); // 여행 리포트 삭제
        deleteStudyLogs(phases); // 학습 로그 삭제
        deleteDailyGoals(phases); // 데일리 목표 삭제
        deleteMissions(phases); // 미션 삭제
        deleteStamps(phases); // 스탬프 삭제
        deleteTrips(phases); // 여행 삭제
        deleteMembers(phases); // 멤버 삭제
    }

    private void deletePomodoros(Map<String, Long> phases) {
        phases.put(
                POMODOROS_OWNED_BY_DELETED_DAILY_GOAL,
                executor.run(
                        POMODOROS_OWNED_BY_DELETED_DAILY_GOAL,
                        pomodoroCommandService::hardDeletePomodorosOwnedByDeletedDailyGoal));
        phases.put(POMODOROS, executor.run(POMODOROS, pomodoroCommandService::hardDeletePomodoros));
    }

    private void deleteStudyLogDailyMissions(Map<String, Long> phases) {
        phases.put(
                STUDY_LOG_DAILY_MISSIONS_OWNED_BY_DELETED_DAILY_MISSION,
                executor.run(
                        STUDY_LOG_DAILY_MISSIONS_OWNED_BY_DELETED_DAILY_MISSION,
                        studyLogDailyMissionCommandService
                                ::hardDeleteStudyLogDailyMissionsOwnedByDeletedDailyMission));
        phases.put(
                STUDY_LOG_DAILY_MISSIONS_OWNED_BY_DELETED_STUDY_LOG,
                executor.run(
                        STUDY_LOG_DAILY_MISSIONS_OWNED_BY_DELETED_STUDY_LOG,
                        studyLogDailyMissionCommandService
                                ::hardDeleteStudyLogDailyMissionsOwnedByDeletedStudyLog));
        phases.put(
                STUDY_LOG_DAILY_MISSIONS,
                executor.run(
                        STUDY_LOG_DAILY_MISSIONS,
                        studyLogDailyMissionCommandService::hardDeleteStudyLogDailyMissions));
    }

    private void deleteDailyMissions(Map<String, Long> phases) {
        phases.put(
                DAILY_MISSIONS_OWNED_BY_DELETED_MISSION,
                executor.run(
                        DAILY_MISSIONS_OWNED_BY_DELETED_MISSION,
                        dailyMissionCommandService::hardDeleteDailyMissionsOwnedByDeletedMission));
        phases.put(
                DAILY_MISSIONS_OWNED_BY_DELETED_DAILY_GOAL,
                executor.run(
                        DAILY_MISSIONS_OWNED_BY_DELETED_DAILY_GOAL,
                        dailyMissionCommandService
                                ::hardDeleteDailyMissionsOwnedByDeletedDailyGoal));
        phases.put(
                DAILY_MISSIONS,
                executor.run(DAILY_MISSIONS, dailyMissionCommandService::hardDeleteDailyMissions));
    }

    private void deleteTripReportStudyLogs(Map<String, Long> phases) {
        phases.put(
                TRIP_REPORT_STUDY_LOGS_OWNED_BY_DELETED_MEMBER,
                executor.run(
                        TRIP_REPORT_STUDY_LOGS_OWNED_BY_DELETED_MEMBER,
                        tripReportStudyLogCommandService
                                ::hardDeleteTripReportStudyLogsOwnedByDeletedMember));
    }

    private void deleteTripReports(Map<String, Long> phases) {
        phases.put(
                TRIP_REPORTS_OWNED_BY_DELETED_MEMBER,
                executor.run(
                        TRIP_REPORTS_OWNED_BY_DELETED_MEMBER,
                        tripReportCommandService::hardDeleteTripReportsOwnedByDeletedMember));
        phases.put(
                TRIP_REPORTS,
                executor.run(TRIP_REPORTS, tripReportCommandService::hardDeleteTripReports));
    }

    private void deleteStudyLogs(Map<String, Long> phases) {
        phases.put(
                STUDY_LOGS_OWNED_BY_DELETED_MEMBER,
                executor.run(
                        STUDY_LOGS_OWNED_BY_DELETED_MEMBER,
                        studyLogCommandService::hardDeleteStudyLogsOwnedByDeletedMember));
        phases.put(
                STUDY_LOGS_OWNED_BY_DELETED_DAILY_GOAL,
                executor.run(
                        STUDY_LOGS_OWNED_BY_DELETED_DAILY_GOAL,
                        studyLogCommandService::hardDeleteStudyLogsOwnedByDeletedDailyGoal));
        phases.put(
                STUDY_LOGS, executor.run(STUDY_LOGS, studyLogCommandService::hardDeleteStudyLogs));
    }

    private void deleteDailyGoals(Map<String, Long> phases) {
        phases.put(
                DAILY_GOALS_OWNED_BY_DELETED_TRIP,
                executor.run(
                        DAILY_GOALS_OWNED_BY_DELETED_TRIP,
                        dailyGoalCommandService::hardDeleteDailyGoalsOwnedByDeletedTrip));
        phases.put(
                DAILY_GOALS,
                executor.run(DAILY_GOALS, dailyGoalCommandService::hardDeleteDailyGoals));
    }

    private void deleteMissions(Map<String, Long> phases) {
        phases.put(
                MISSIONS_OWNED_BY_DELETED_STAMP,
                executor.run(
                        MISSIONS_OWNED_BY_DELETED_STAMP,
                        missionCommandService::hardDeleteMissionsOwnedByDeletedStamp));
        phases.put(MISSIONS, executor.run(MISSIONS, missionCommandService::hardDeleteMissions));
    }

    private void deleteStamps(Map<String, Long> phases) {
        phases.put(
                STAMPS_OWNED_BY_DELETED_TRIP,
                executor.run(
                        STAMPS_OWNED_BY_DELETED_TRIP,
                        stampCommandService::hardDeleteStampsOwnedByDeletedTrip));
        phases.put(STAMPS, executor.run(STAMPS, stampCommandService::hardDeleteStamps));
    }

    private void deleteTrips(Map<String, Long> phases) {
        phases.put(
                TRIPS_OWNED_BY_DELETED_MEMBER,
                executor.run(
                        TRIPS_OWNED_BY_DELETED_MEMBER,
                        tripCommandService::hardDeleteTripsOwnedByDeletedMember));
        phases.put(TRIPS, executor.run(TRIPS, tripCommandService::hardDeleteTrips));
    }

    private void deleteMembers(Map<String, Long> phases) {
        phases.put(MEMBERS, executor.run(MEMBERS, memberCommandService::hardDeleteMembers));
    }
}
