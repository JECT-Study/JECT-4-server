package com.ject.studytrip.trip.domain.model;

import static org.springframework.util.StringUtils.hasText;

import com.ject.studytrip.global.common.entity.BaseTimeEntity;
import com.ject.studytrip.member.domain.model.Member;
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
public class Trip extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private String name;

    private String memo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TripCategory category;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    private int totalStamps;

    private int completedStamps;

    private boolean completed;

    public static Trip of(
            Member member,
            String name,
            String memo,
            TripCategory category,
            LocalDate endDate,
            int totalStamps) {
        return Trip.builder()
                .member(member)
                .name(name)
                .memo(memo)
                .category(category)
                .startDate(LocalDate.now())
                .endDate(endDate)
                .totalStamps(totalStamps)
                .completedStamps(0)
                .completed(false)
                .build();
    }

    public void update(String name, String memo, TripCategory category, LocalDate endDate) {
        if (hasText(name)) this.name = name;
        if (hasText(memo)) this.memo = memo;
        if (Objects.nonNull(category)) this.category = category;
        if (Objects.nonNull(endDate)) this.endDate = endDate;
    }

    //    public void updateName(String name) {
    //        if (hasText(name)) this.name = name;
    //    }
    //
    //    public void updateMemo(String memo) { if (hasText(memo)) this.memo = memo; }
    //
    //    public void updateCategory(TripCategory category) {
    //        if (Objects.nonNull(category)) this.category = category;
    //    }
    //
    //    public void updateDeadline(LocalDate deadline) {
    //        if (Objects.nonNull(deadline)) this.deadline = deadline;
    //    }

    public void increaseTotalStamps() {
        this.totalStamps += 1;
    }

    public void decreaseTotalStamps() {
        this.totalStamps -= 1;
    }

    public void updateCompleted() {
        this.completed = true;
    }

    public void updateDeletedAt() {
        this.deletedAt = LocalDateTime.now();
    }

    public void increaseCompletedStamps() {
        this.completedStamps += 1;
    }
}
