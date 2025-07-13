package com.ject.studytrip.mission.domain.model;

import com.ject.studytrip.global.common.entity.BaseTimeEntity;
import com.ject.studytrip.stamp.domain.model.Stamp;
import jakarta.persistence.*;
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

    private int missionOrder;

    private boolean completed;

    public static Mission of(Stamp stamp, String name, int missionOrder) {
        return Mission.builder()
                .stamp(stamp)
                .name(name)
                .missionOrder(missionOrder)
                .completed(false)
                .build();
    }
}
