package com.ject.studytrip.stamp.domain.model;

import static org.springframework.util.StringUtils.hasText;

import com.ject.studytrip.global.common.entity.BaseTimeEntity;
import com.ject.studytrip.trip.domain.model.Trip;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder(access = AccessLevel.PRIVATE)
public class Stamp extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @Column(nullable = false)
    private String name;

    private int stampOrder;

    private boolean completed;

    public static Stamp of(Trip trip, String name, int stampOrder) {
        return Stamp.builder()
                .trip(trip)
                .name(name)
                .stampOrder(stampOrder)
                .completed(false)
                .build();
    }

    public void updateName(String name) {
        if (hasText(name)) this.name = name;
    }

    public void updateStampOrder(int newOrder) {
        this.stampOrder = newOrder;
    }

    public void updateCompleted() {
        this.completed = true;
    }

    public void updateDeletedAt() {
        this.deletedAt = LocalDateTime.now();
    }
}
