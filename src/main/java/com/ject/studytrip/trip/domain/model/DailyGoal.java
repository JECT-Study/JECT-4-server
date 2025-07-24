package com.ject.studytrip.trip.domain.model;

import com.ject.studytrip.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder(access = AccessLevel.PRIVATE)
public class DailyGoal extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    private boolean completed;

    public static DailyGoal of(Trip trip) {
        return DailyGoal.builder().trip(trip).completed(false).build();
    }

    public void updateDeletedAt() {
        this.deletedAt = LocalDateTime.now();
    }
}
