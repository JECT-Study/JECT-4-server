package com.ject.studytrip.mission.domain.model;

import com.ject.studytrip.global.common.entity.BaseTimeEntity;
import com.ject.studytrip.trip.domain.model.DailyGoal;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class DailyMission extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    private Mission mission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_goal_id", nullable = false)
    private DailyGoal dailyGoal;

    public static DailyMission of(Mission mission, DailyGoal dailyGoal) {
        return DailyMission.builder().mission(mission).dailyGoal(dailyGoal).build();
    }

    public void updateDeletedAt() {
        this.deletedAt = LocalDateTime.now();
    }

    public DailyGoal getDailyGoal() {
        return dailyGoal;
    }

    public Long getId() {
        return id;
    }

    public Mission getMission() {
        return mission;
    }
}
