package com.ject.studytrip.trip.domain.model;

import static org.flywaydb.core.internal.util.StringUtils.hasText;

import com.ject.studytrip.global.common.entity.BaseTimeEntity;
import com.ject.studytrip.member.domain.model.Member;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class TripReport extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column private String title;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private String startDate;

    private String endDate;

    @Column(nullable = false)
    private long studyLogCount;

    @Column(nullable = false)
    private long totalFocusHours;

    @Column(nullable = false)
    private long studyDays;

    private String imageTitle;

    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    public static TripReport of(
            Member member,
            String title,
            String content,
            String startDate,
            String endDate,
            long studyLogCount,
            long totalFocusHours,
            long studyDays,
            String imageTitle) {
        return TripReport.builder()
                .title(title)
                .content(content)
                .startDate(startDate)
                .endDate(endDate)
                .studyLogCount(studyLogCount)
                .totalFocusHours(totalFocusHours)
                .studyDays(studyDays)
                .imageTitle(imageTitle)
                .imageUrl(null)
                .member(member)
                .build();
    }

    public void updateImageUrl(String imageUrl) {
        if (hasText(imageUrl)) this.imageUrl = imageUrl;
    }

    public void updateDeletedAt() {
        this.deletedAt = LocalDateTime.now();
    }

    public long getTotalFocusHours() {
        return totalFocusHours;
    }

    public String getTitle() {
        return title;
    }

    public long getStudyLogCount() {
        return studyLogCount;
    }

    public long getStudyDays() {
        return studyDays;
    }

    public String getStartDate() {
        return startDate;
    }

    public Member getMember() {
        return member;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getImageTitle() {
        return imageTitle;
    }

    public Long getId() {
        return id;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getContent() {
        return content;
    }
}
