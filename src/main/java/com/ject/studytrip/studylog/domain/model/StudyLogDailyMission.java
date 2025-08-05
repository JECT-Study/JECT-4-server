package com.ject.studytrip.studylog.domain.model;

import com.ject.studytrip.global.common.entity.BaseTimeEntity;
import com.ject.studytrip.mission.domain.model.DailyMission;
import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder(access = AccessLevel.PRIVATE)
public class StudyLogDailyMission extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_log_id", nullable = false)
    private StudyLog studyLog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_mission_id", nullable = false)
    private DailyMission dailyMission;

    public static StudyLogDailyMission of(StudyLog studyLog, DailyMission dailyMission) {
        return StudyLogDailyMission.builder().studyLog(studyLog).dailyMission(dailyMission).build();
    }
}
