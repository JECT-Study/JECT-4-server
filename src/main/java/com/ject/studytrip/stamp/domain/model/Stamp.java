package com.ject.studytrip.stamp.domain.model;

import com.ject.studytrip.global.common.entity.BaseTimeEntity;
import com.ject.studytrip.trip.domain.model.Trip;
import jakarta.persistence.*;
import java.time.LocalDate;
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

    @Column(nullable = false)
    private LocalDate deadline;

    private boolean completed;

    public static Stamp of(Trip trip, String name, int stampOrder, LocalDate deadline) {
        return Stamp.builder()
                .trip(trip)
                .name(name)
                .stampOrder(stampOrder)
                .deadline(deadline)
                .completed(false)
                .build();
    }

    public void updateStampOrder(int newOrder) {
        this.stampOrder = newOrder;
    }
}
