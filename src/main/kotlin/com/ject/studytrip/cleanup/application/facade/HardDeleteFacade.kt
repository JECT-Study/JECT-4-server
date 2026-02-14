package com.ject.studytrip.cleanup.application.facade

import com.ject.studytrip.cleanup.application.executor.HardDeleteExecutor
import com.ject.studytrip.member.application.service.MemberCommandService
import com.ject.studytrip.mission.application.service.DailyMissionCommandService
import com.ject.studytrip.mission.application.service.MissionCommandService
import com.ject.studytrip.pomodoro.application.service.PomodoroCommandService
import com.ject.studytrip.stamp.application.service.StampCommandService
import com.ject.studytrip.studylog.application.service.StudyLogCommandService
import com.ject.studytrip.studylog.application.service.StudyLogDailyMissionCommandService
import com.ject.studytrip.trip.application.service.DailyGoalCommandService
import com.ject.studytrip.trip.application.service.TripCommandService
import com.ject.studytrip.trip.application.service.TripReportCommandService
import com.ject.studytrip.trip.application.service.TripReportStudyLogCommandService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.LinkedHashMap

@Component
class HardDeleteFacade(
    private val memberCommandService: MemberCommandService,
    private val tripCommandService: TripCommandService,
    private val stampCommandService: StampCommandService,
    private val missionCommandService: MissionCommandService,
    private val studyLogCommandService: StudyLogCommandService,
    private val dailyMissionCommandService: DailyMissionCommandService,
    private val studyLogDailyMissionCommandService: StudyLogDailyMissionCommandService,
    private val dailyGoalCommandService: DailyGoalCommandService,
    private val pomodoroCommandService: PomodoroCommandService,
    private val tripReportCommandService: TripReportCommandService,
    private val tripReportStudyLogCommandService: TripReportStudyLogCommandService,
    private val executor: HardDeleteExecutor,
) {
    companion object {
        private const val POMODOROS_OWNED_BY_DELETED_DAILY_GOAL = "pomodorosOwnedByDeletedDailyGoal"
        private const val STUDY_LOG_DAILY_MISSIONS_OWNED_BY_DELETED_DAILY_MISSION = "studyLogDailyMissionsOwnedByDeletedDailyMission"
        private const val STUDY_LOG_DAILY_MISSIONS_OWNED_BY_DELETED_STUDY_LOG = "studyLogDailyMissionsOwnedByDeletedStudyLog"
        private const val STUDY_LOGS_OWNED_BY_DELETED_MEMBER = "studyLogsOwnedByDeletedMember"
        private const val STUDY_LOGS_OWNED_BY_DELETED_DAILY_GOAL = "studyLogsOwnedByDeletedDailyGoal"
        private const val DAILY_MISSIONS_OWNED_BY_DELETED_MISSION = "dailyMissionsOwnedByDeletedMission"
        private const val DAILY_MISSIONS_OWNED_BY_DELETED_DAILY_GOAL = "dailyMissionsOwnedByDeletedDailyGoal"
        private const val TRIP_REPORT_STUDY_LOGS_OWNED_BY_DELETED_MEMBER = "tripReportStudyLogsOwnedByDeletedMember"
        private const val TRIP_REPORTS_OWNED_BY_DELETED_MEMBER = "tripReportsOwnedByDeletedMember"

        private const val POMODOROS = "pomodoros"
        private const val STUDY_LOG_DAILY_MISSIONS = "studyLogDailyMissions"
        private const val STUDY_LOGS = "studyLogs"
        private const val DAILY_MISSIONS = "dailyMissions"
        private const val MISSIONS_OWNED_BY_DELETED_STAMP = "missionsOwnedByDeletedStamp"
        private const val MISSIONS = "missions"
        private const val STAMPS_OWNED_BY_DELETED_TRIP = "stampsOwnedByDeletedTrip"
        private const val STAMPS = "stamps"
        private const val TRIPS_OWNED_BY_DELETED_MEMBER = "tripsOwnedByDeletedMember"
        private const val TRIPS = "trips"
        private const val DAILY_GOALS_OWNED_BY_DELETED_TRIP = "dailyGoalsOwnedByDeletedTrip"
        private const val DAILY_GOALS = "dailyGoals"
        private const val TRIP_REPORTS = "tripReports"
        private const val MEMBERS = "members"
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun hardDeleteAll() {
        val phases = LinkedHashMap<String, Long>()

        deletePomodoros(phases) // 뽀모도로 삭제
        deleteStudyLogDailyMissions(phases) // StudyLogDailyMission 삭제
        deleteDailyMissions(phases) // 데일리 미션 삭제
        deleteTripReportStudyLogs(phases) // TripReportStudyLog 삭제
        deleteTripReports(phases) // 여행 리포트 삭제
        deleteStudyLogs(phases) // 학습 로그 삭제
        deleteDailyGoals(phases) // 데일리 목표 삭제
        deleteMissions(phases) // 미션 삭제
        deleteStamps(phases) // 스탬프 삭제
        deleteTrips(phases) // 여행 삭제
        deleteMembers(phases) // 멤버 삭제
    }

    private fun deletePomodoros(phases: MutableMap<String, Long>) {
        phases[POMODOROS_OWNED_BY_DELETED_DAILY_GOAL] =
            executor.run(POMODOROS_OWNED_BY_DELETED_DAILY_GOAL) {
                pomodoroCommandService.hardDeletePomodorosOwnedByDeletedDailyGoal()
            }
        phases[POMODOROS] =
            executor.run(POMODOROS) {
                pomodoroCommandService.hardDeletePomodoros()
            }
    }

    private fun deleteStudyLogDailyMissions(phases: MutableMap<String, Long>) {
        phases[STUDY_LOG_DAILY_MISSIONS_OWNED_BY_DELETED_DAILY_MISSION] =
            executor.run(STUDY_LOG_DAILY_MISSIONS_OWNED_BY_DELETED_DAILY_MISSION) {
                studyLogDailyMissionCommandService.hardDeleteStudyLogDailyMissionsOwnedByDeletedDailyMission()
            }

        phases[STUDY_LOG_DAILY_MISSIONS_OWNED_BY_DELETED_STUDY_LOG] =
            executor.run(STUDY_LOG_DAILY_MISSIONS_OWNED_BY_DELETED_STUDY_LOG) {
                studyLogDailyMissionCommandService.hardDeleteStudyLogDailyMissionsOwnedByDeletedStudyLog()
            }

        phases[STUDY_LOG_DAILY_MISSIONS] =
            executor.run(STUDY_LOG_DAILY_MISSIONS) {
                studyLogDailyMissionCommandService.hardDeleteStudyLogDailyMissions()
            }
    }

    private fun deleteDailyMissions(phases: MutableMap<String, Long>) {
        phases[DAILY_MISSIONS_OWNED_BY_DELETED_MISSION] =
            executor.run(DAILY_MISSIONS_OWNED_BY_DELETED_MISSION) {
                dailyMissionCommandService.hardDeleteDailyMissionsOwnedByDeletedMission()
            }

        phases[DAILY_MISSIONS_OWNED_BY_DELETED_DAILY_GOAL] =
            executor.run(DAILY_MISSIONS_OWNED_BY_DELETED_DAILY_GOAL) {
                dailyMissionCommandService.hardDeleteDailyMissionsOwnedByDeletedDailyGoal()
            }

        phases[DAILY_MISSIONS] =
            executor.run(DAILY_MISSIONS) {
                dailyMissionCommandService.hardDeleteDailyMissions()
            }
    }

    private fun deleteTripReportStudyLogs(phases: MutableMap<String, Long>) {
        phases[TRIP_REPORT_STUDY_LOGS_OWNED_BY_DELETED_MEMBER] =
            executor.run(TRIP_REPORT_STUDY_LOGS_OWNED_BY_DELETED_MEMBER) {
                tripReportStudyLogCommandService.hardDeleteTripReportStudyLogsOwnedByDeletedMember()
            }
    }

    private fun deleteTripReports(phases: MutableMap<String, Long>) {
        phases[TRIP_REPORTS_OWNED_BY_DELETED_MEMBER] =
            executor.run(TRIP_REPORTS_OWNED_BY_DELETED_MEMBER) {
                tripReportCommandService.hardDeleteTripReportsOwnedByDeletedMember()
            }

        phases[TRIP_REPORTS] =
            executor.run(TRIP_REPORTS) {
                tripReportCommandService.hardDeleteTripReports()
            }
    }

    private fun deleteStudyLogs(phases: MutableMap<String, Long>) {
        phases[STUDY_LOGS_OWNED_BY_DELETED_MEMBER] =
            executor.run(STUDY_LOGS_OWNED_BY_DELETED_MEMBER) {
                studyLogCommandService.hardDeleteStudyLogsOwnedByDeletedMember()
            }

        phases[STUDY_LOGS_OWNED_BY_DELETED_DAILY_GOAL] =
            executor.run(STUDY_LOGS_OWNED_BY_DELETED_DAILY_GOAL) {
                studyLogCommandService.hardDeleteStudyLogsOwnedByDeletedDailyGoal()
            }

        phases[STUDY_LOGS] =
            executor.run(STUDY_LOGS) {
                studyLogCommandService.hardDeleteStudyLogs()
            }
    }

    private fun deleteDailyGoals(phases: MutableMap<String, Long>) {
        phases[DAILY_GOALS_OWNED_BY_DELETED_TRIP] =
            executor.run(DAILY_GOALS_OWNED_BY_DELETED_TRIP) {
                dailyGoalCommandService.hardDeleteDailyGoalsOwnedByDeletedTrip()
            }

        phases[DAILY_GOALS] =
            executor.run(DAILY_GOALS) {
                dailyGoalCommandService.hardDeleteDailyGoals()
            }
    }

    private fun deleteMissions(phases: MutableMap<String, Long>) {
        phases[MISSIONS_OWNED_BY_DELETED_STAMP] =
            executor.run(MISSIONS_OWNED_BY_DELETED_STAMP) {
                missionCommandService.hardDeleteMissionsOwnedByDeletedStamp()
            }

        phases[MISSIONS] =
            executor.run(MISSIONS) {
                missionCommandService.hardDeleteMissions()
            }
    }

    private fun deleteStamps(phases: MutableMap<String, Long>) {
        phases[STAMPS_OWNED_BY_DELETED_TRIP] =
            executor.run(STAMPS_OWNED_BY_DELETED_TRIP) {
                stampCommandService.hardDeleteStampsOwnedByDeletedTrip()
            }

        phases[STAMPS] =
            executor.run(STAMPS) {
                stampCommandService.hardDeleteStamps()
            }
    }

    private fun deleteTrips(phases: MutableMap<String, Long>) {
        phases[TRIPS_OWNED_BY_DELETED_MEMBER] =
            executor.run(TRIPS_OWNED_BY_DELETED_MEMBER) {
                tripCommandService.hardDeleteTripsOwnedByDeletedMember()
            }

        phases[TRIPS] =
            executor.run(TRIPS) {
                tripCommandService.hardDeleteTrips()
            }
    }

    private fun deleteMembers(phases: MutableMap<String, Long>) {
        phases[MEMBERS] =
            executor.run(MEMBERS) {
                memberCommandService.hardDeleteMembers()
            }
    }
}
