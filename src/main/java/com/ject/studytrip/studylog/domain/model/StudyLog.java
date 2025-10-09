package com.ject.studytrip.studylog.domain.model;

import static org.flywaydb.core.internal.util.StringUtils.hasText;

import com.ject.studytrip.global.common.entity.BaseTimeEntity;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.trip.domain.model.DailyGoal;
import jakarta.persistence.*;
import java.time.LocalDateTime;
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
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_goal_id", nullable = false)
    private DailyGoal dailyGoal;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    private String imageUrl;

    public static StudyLog of(Member member, DailyGoal dailyGoal, String content) {
        return StudyLog.builder()
                .member(member)
                .dailyGoal(dailyGoal)
                .title(dailyGoal.getTitle())
                .content(content)
                .imageUrl(null)
                .build();
    }

    public void updateImageUrl(String imageUrl) {
        if (hasText(imageUrl)) this.imageUrl = imageUrl;
    }

    public void updateDeletedAt() {
        this.deletedAt = LocalDateTime.now();
    }
}
