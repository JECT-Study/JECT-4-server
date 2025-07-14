package com.ject.studytrip.stamp.domain.model;

import static org.springframework.util.StringUtils.hasText;

import com.ject.studytrip.global.common.entity.BaseTimeEntity;
import com.ject.studytrip.trip.domain.model.Trip;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
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

    public void update(String name, LocalDate deadline) {
        if (hasText(name)) this.name = name;
        if (Objects.nonNull(deadline)) this.deadline = deadline;
    }

    public void updateStampOrder(int newOrder) {
        this.stampOrder = newOrder;
    }

    public void updateDeletedAt() {
        this.deletedAt = LocalDateTime.now();
    }
}
