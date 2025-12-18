package com.ject.studytrip.trip.presentation.dto.response;

import com.ject.studytrip.mission.application.dto.DailyMissionInfo;
import com.ject.studytrip.pomodoro.application.dto.PomodoroInfo;
import com.ject.studytrip.trip.application.dto.DailyGoalInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record LoadDailyGoalDetailResponse(
        @Schema(name = "데일리 목표 ID") Long dailyGoalId,
        @Schema(name = "데일리 목표 제목(스탬프 이름)") String title,
        @Schema(name = "데일리 목표 완료 여부") boolean completed,
        @Schema(name = "뽀모도로 정보") DailyGoalPomodoroResponse pomodoro,
        @Schema(name = "수행할 데일리 미션 목록") List<DailyGoalMissionResponse> dailyMissions) {

    public static LoadDailyGoalDetailResponse of(
            DailyGoalInfo dailyGoalInfo,
            PomodoroInfo pomodoroInfo,
            List<DailyMissionInfo> dailyMissionInfos) {
        return new LoadDailyGoalDetailResponse(
                dailyGoalInfo.dailyGoalId(),
                dailyGoalInfo.title(),
                dailyGoalInfo.completed(),
                DailyGoalPomodoroResponse.of(pomodoroInfo),
                dailyMissionInfos.stream().map(DailyGoalMissionResponse::of).toList());
    }

    public record DailyGoalPomodoroResponse(
            @Schema(name = "뽀모도로 ID") Long pomodoroId,
            @Schema(name = "뽀모도로 집중 시간(분)") int focusDurationInMinute,
            @Schema(name = "뽀모도로 집중 세션 개수") int focusSessionCount) {
        public static DailyGoalPomodoroResponse of(PomodoroInfo info) {
            return new DailyGoalPomodoroResponse(
                    info.getPomodoroId(),
                    info.getFocusDurationInMinute(),
                    info.getFocusSessionCount());
        }
    }

    public record DailyGoalMissionResponse(
            @Schema(name = "데일리 미션 ID") Long dailyMissionId,
            @Schema(name = "미션 이름") String missionName) {
        public static DailyGoalMissionResponse of(DailyMissionInfo info) {
            return new DailyGoalMissionResponse(
                    info.getDailyMissionId(), info.getMissionInfo().getMissionName());
        }
    }
}
