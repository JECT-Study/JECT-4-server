package com.ject.studytrip.pomodoro.domain.model;

import com.ject.studytrip.global.common.entity.BaseTimeEntity;
import com.ject.studytrip.trip.domain.model.DailyGoal;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder(access = AccessLevel.PRIVATE)
public class Pomodoro extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_goal_id", nullable = false)
    private DailyGoal dailyGoal;

    private int focusDurationInSeconds; // 집중 시간(초)
    private int focusSessionCount; // 집중 세션 횟수
    private int breakDurationInSeconds; // 휴식 시간(초)
    private int totalFocusTimeInSeconds; // 총 집중 시간(초)

    // TODO : 추후 집중 세션 개수와 휴식 시간을 설정할 수 있는 기능이 추가될 경우 리팩토링
    // 우선은 고정 값 세팅
    public static Pomodoro of(
            DailyGoal dailyGoal,
            int focusDurationInSeconds,
            int focusSessionCount,
            int breakDurationInSeconds) {
        return Pomodoro.builder()
                .dailyGoal(dailyGoal)
                .focusDurationInSeconds(focusDurationInSeconds)
                .focusSessionCount(focusSessionCount)
                .breakDurationInSeconds(0)
                .totalFocusTimeInSeconds(0)
                .build();
    }

    public void updateTotalFocusTimeInSeconds(int totalFocusTimeInSeconds) {
        this.totalFocusTimeInSeconds = totalFocusTimeInSeconds;
    }

    public void updateDeletedAt() {
        this.deletedAt = LocalDateTime.now();
    }
}
