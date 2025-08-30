package com.ject.studytrip.cleanup.application.facade;

import com.ject.studytrip.cleanup.application.executor.HardDeleteExecutor;
import com.ject.studytrip.member.application.service.MemberService;
import com.ject.studytrip.mission.application.service.DailyMissionService;
import com.ject.studytrip.mission.application.service.MissionService;
import com.ject.studytrip.pomodoro.application.service.PomodoroService;
import com.ject.studytrip.stamp.application.service.StampService;
import com.ject.studytrip.studylog.application.service.StudyLogDailyMissionService;
import com.ject.studytrip.studylog.application.service.StudyLogService;
import com.ject.studytrip.trip.application.service.DailyGoalService;
import com.ject.studytrip.trip.application.service.TripService;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HardDeleteFacade {
    private final PomodoroService pomodoroService;
    private final StudyLogDailyMissionService studyLogDailyMissionService;
    private final StudyLogService studyLogService;
    private final DailyMissionService dailyMissionService;
    private final MissionService missionService;
    private final StampService stampService;
    private final TripService tripService;
    private final DailyGoalService dailyGoalService;
    private final MemberService memberService;

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
    private static final String MEMBERS = "members";

    public void hardDeleteAll() {
        Map<String, Long> phases = new LinkedHashMap<>();

        deletePomodoros(phases); // 뽀모도로 삭제
        deleteStudyLogDailyMissions(phases); // StudyLogDailyMission 삭제
        deleteDailyMissions(phases); // 데일리 미션 삭제
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
                        pomodoroService::hardDeletePomodorosOwnedByDeletedDailyGoal));
        phases.put(POMODOROS, executor.run(POMODOROS, pomodoroService::hardDeletePomodoros));
    }

    private void deleteStudyLogDailyMissions(Map<String, Long> phases) {
        phases.put(
                STUDY_LOG_DAILY_MISSIONS_OWNED_BY_DELETED_DAILY_MISSION,
                executor.run(
                        STUDY_LOG_DAILY_MISSIONS_OWNED_BY_DELETED_DAILY_MISSION,
                        studyLogDailyMissionService
                                ::hardDeleteStudyLogDailyMissionsOwnedByDeletedDailyMission));
        phases.put(
                STUDY_LOG_DAILY_MISSIONS_OWNED_BY_DELETED_STUDY_LOG,
                executor.run(
                        STUDY_LOG_DAILY_MISSIONS_OWNED_BY_DELETED_STUDY_LOG,
                        studyLogDailyMissionService
                                ::hardDeleteStudyLogDailyMissionsOwnedByDeletedStudyLog));
        phases.put(
                STUDY_LOG_DAILY_MISSIONS,
                executor.run(
                        STUDY_LOG_DAILY_MISSIONS,
                        studyLogDailyMissionService::hardDeleteStudyLogDailyMissions));
    }

    private void deleteDailyMissions(Map<String, Long> phases) {
        phases.put(
                DAILY_MISSIONS_OWNED_BY_DELETED_MISSION,
                executor.run(
                        DAILY_MISSIONS_OWNED_BY_DELETED_MISSION,
                        dailyMissionService::hardDeleteDailyMissionsOwnedByDeletedMission));
        phases.put(
                DAILY_MISSIONS_OWNED_BY_DELETED_DAILY_GOAL,
                executor.run(
                        DAILY_MISSIONS_OWNED_BY_DELETED_DAILY_GOAL,
                        dailyMissionService::hardDeleteDailyMissionsOwnedByDeletedDailyGoal));
        phases.put(
                DAILY_MISSIONS,
                executor.run(DAILY_MISSIONS, dailyMissionService::hardDeleteDailyMissions));
    }

    private void deleteStudyLogs(Map<String, Long> phases) {
        phases.put(
                STUDY_LOGS_OWNED_BY_DELETED_MEMBER,
                executor.run(
                        STUDY_LOGS_OWNED_BY_DELETED_MEMBER,
                        studyLogService::hardDeleteStudyLogsOwnedByDeletedMember));
        phases.put(
                STUDY_LOGS_OWNED_BY_DELETED_DAILY_GOAL,
                executor.run(
                        STUDY_LOGS_OWNED_BY_DELETED_DAILY_GOAL,
                        studyLogService::hardDeleteStudyLogsOwnedByDeletedDailyGoal));
        phases.put(STUDY_LOGS, executor.run(STUDY_LOGS, studyLogService::hardDeleteStudyLogs));
    }

    private void deleteDailyGoals(Map<String, Long> phases) {
        phases.put(
                DAILY_GOALS_OWNED_BY_DELETED_TRIP,
                executor.run(
                        DAILY_GOALS_OWNED_BY_DELETED_TRIP,
                        dailyGoalService::hardDeleteDailyGoalsOwnedByDeletedTrip));
        phases.put(DAILY_GOALS, executor.run(DAILY_GOALS, dailyGoalService::hardDeleteDailyGoals));
    }

    private void deleteMissions(Map<String, Long> phases) {
        phases.put(
                MISSIONS_OWNED_BY_DELETED_STAMP,
                executor.run(
                        MISSIONS_OWNED_BY_DELETED_STAMP,
                        missionService::hardDeleteMissionsOwnedByDeletedStamp));
        phases.put(MISSIONS, executor.run(MISSIONS, missionService::hardDeleteMissions));
    }

    private void deleteStamps(Map<String, Long> phases) {
        phases.put(
                STAMPS_OWNED_BY_DELETED_TRIP,
                executor.run(
                        STAMPS_OWNED_BY_DELETED_TRIP,
                        stampService::hardDeleteStampsOwnedByDeletedTrip));
        phases.put(STAMPS, executor.run(STAMPS, stampService::hardDeleteStamps));
    }

    private void deleteTrips(Map<String, Long> phases) {
        phases.put(
                TRIPS_OWNED_BY_DELETED_MEMBER,
                executor.run(
                        TRIPS_OWNED_BY_DELETED_MEMBER,
                        tripService::hardDeleteTripsOwnedByDeletedMember));
        phases.put(TRIPS, executor.run(TRIPS, tripService::hardDeleteTrips));
    }

    private void deleteMembers(Map<String, Long> phases) {
        phases.put(MEMBERS, executor.run(MEMBERS, memberService::hardDeleteMembers));
    }
}
