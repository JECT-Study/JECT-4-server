package com.ject.studytrip.trip.presentation.dto.response

import com.ject.studytrip.mission.application.dto.DailyMissionInfo
import com.ject.studytrip.pomodoro.application.dto.PomodoroInfo
import com.ject.studytrip.trip.application.dto.DailyGoalInfo
import io.swagger.v3.oas.annotations.media.Schema

data class LoadDailyGoalDetailResponse(
    @field:Schema(description = "데일리 목표 ID")
    val dailyGoalId: Long,
    @field:Schema(description = "데일리 목표 제목 (스탬프 이름)")
    val title: String,
    @field:Schema(description = "데일리 목표 완료 여부")
    val completed: Boolean,
    @field:Schema(description = "뽀모도로 정보")
    val pomodoro: DailyGoalPomodoroResponse,
    @field:Schema(description = "수행할 데일리 미션 목록")
    val dailyMissions: List<DailyGoalMissionResponse>,
) {
    companion object {
        fun of(
            dailyGoalInfo: DailyGoalInfo,
            pomodoroInfo: PomodoroInfo,
            dailyMissionInfos: List<DailyMissionInfo>,
        ): LoadDailyGoalDetailResponse =
            LoadDailyGoalDetailResponse(
                dailyGoalInfo.dailyGoalId,
                dailyGoalInfo.title,
                dailyGoalInfo.completed,
                DailyGoalPomodoroResponse.of(pomodoroInfo),
                dailyMissionInfos.map(DailyGoalMissionResponse::of),
            )
    }

    data class DailyGoalPomodoroResponse(
        @field:Schema(description = "뽀모도로 ID")
        val pomodoroId: Long,
        @field:Schema(description = "뽀모도로 집중 시간(분)")
        val focusDurationInMinute: Int,
        @field:Schema(description = "뽀모도로 집중 세션 개수")
        val focusSessionCount: Int,
    ) {
        companion object {
            fun of(pomodoroInfo: PomodoroInfo): DailyGoalPomodoroResponse =
                DailyGoalPomodoroResponse(
                    pomodoroInfo.pomodoroId,
                    pomodoroInfo.focusDurationInMinute,
                    pomodoroInfo.focusSessionCount,
                )
        }
    }

    data class DailyGoalMissionResponse(
        @field:Schema(description = "데일리 미션 ID")
        val dailyMissionId: Long,
        @field:Schema(description = "미션 이름")
        val missionName: String,
    ) {
        companion object {
            fun of(dailyMissionInfo: DailyMissionInfo): DailyGoalMissionResponse =
                DailyGoalMissionResponse(
                    dailyMissionInfo.dailyMissionId,
                    dailyMissionInfo.missionInfo.missionName,
                )
        }
    }
}
