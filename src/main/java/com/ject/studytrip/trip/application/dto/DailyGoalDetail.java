package com.ject.studytrip.trip.application.dto;

import com.ject.studytrip.mission.application.dto.DailyMissionInfo;
import com.ject.studytrip.pomodoro.application.dto.PomodoroInfo;
import java.util.List;

public record DailyGoalDetail(
        DailyGoalInfo dailyGoalInfo,
        PomodoroInfo pomodoroInfo,
        List<DailyMissionInfo> dailyMissionInfos) {
    public static DailyGoalDetail from(
            DailyGoalInfo dailyGoalInfo,
            PomodoroInfo pomodoroInfo,
            List<DailyMissionInfo> dailyMissionInfos) {
        return new DailyGoalDetail(dailyGoalInfo, pomodoroInfo, dailyMissionInfos);
    }
}
