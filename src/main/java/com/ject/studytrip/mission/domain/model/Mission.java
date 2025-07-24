package com.ject.studytrip.mission.domain.model;

import static org.springframework.util.StringUtils.hasText;

import com.ject.studytrip.global.common.entity.BaseTimeEntity;
import com.ject.studytrip.stamp.domain.model.Stamp;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder(access = AccessLevel.PRIVATE)
public class Mission extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stamp_id", nullable = false)
    private Stamp stamp;

    @Column(nullable = false)
    private String name;

    private String memo;

    private int missionOrder;

    private boolean completed;

    public static Mission of(Stamp stamp, String name, String memo, int missionOrder) {
        return Mission.builder()
                .stamp(stamp)
                .name(name)
                .memo(memo)
                .missionOrder(missionOrder)
                .completed(false)
                .build();
    }

    public void update(String name, String memo) {
        if (hasText(name)) {
            this.name = name;
        }
        if (hasText(memo)) {
            this.memo = memo;
        }
    }

    public void updateMissionOrder(int missionOrder) {
        this.missionOrder = missionOrder;
    }

    public void updateDeletedAt() {
        this.deletedAt = LocalDateTime.now();
    }

    public void updateCompleted() {
        this.completed = true;
    }
}
