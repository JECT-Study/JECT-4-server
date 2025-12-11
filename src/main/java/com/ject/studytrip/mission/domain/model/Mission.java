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

    private boolean completed;

    public static Mission of(Stamp stamp, String name) {
        return Mission.builder().stamp(stamp).name(name).completed(false).build();
    }

    public void updateName(String name) {
        if (hasText(name)) this.name = name;
    }

    public void updateDeletedAt() {
        this.deletedAt = LocalDateTime.now();
    }

    public void updateCompleted() {
        this.completed = true;
    }

    public boolean isCompleted() {
        return completed;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Stamp getStamp() {
        return stamp;
    }
}
