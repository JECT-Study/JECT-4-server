package com.ject.studytrip.trip.presentation.dto.request;

import com.ject.studytrip.pomodoro.presentation.dto.request.CreatePomodoroRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateDailyGoalRequest(
        @Schema(name = "뽀모도로") @Valid @NotNull(message = "뽀모도로 정보는 필수 요청 값입니다.")
                CreatePomodoroRequest pomodoro,
        @Schema(name = "수행할 미션 ID 목록") @NotEmpty(message = "수행할 미션 목록은 필수 요청 값입니다.")
                List<Long> missionIds) {}
