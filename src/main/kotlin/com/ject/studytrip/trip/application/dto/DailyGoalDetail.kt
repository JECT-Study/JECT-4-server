package com.ject.studytrip.trip.application.dto

import com.ject.studytrip.mission.application.dto.DailyMissionInfo
import com.ject.studytrip.pomodoro.application.dto.PomodoroInfo

data class DailyGoalDetail(
    val dailyGoalInfo: DailyGoalInfo,
    val pomodoroInfo: PomodoroInfo,
    val dailyMissionInfos: List<DailyMissionInfo>,
)
