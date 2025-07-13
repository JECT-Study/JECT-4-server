package com.ject.studytrip.studylog.domain.model;

import com.ject.studytrip.global.common.entity.BaseTimeEntity;
import com.ject.studytrip.trip.domain.model.DailyGoal;
import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder(access = AccessLevel.PRIVATE)
public class StudyLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_goal_id", nullable = false)
    private DailyGoal dailyGoal;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    public static StudyLog of(DailyGoal dailyGoal, String title, String content) {
        return StudyLog.builder().dailyGoal(dailyGoal).title(title).content(content).build();
    }
}
