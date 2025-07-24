package com.ject.studytrip.member.domain.model;

import static org.springframework.util.StringUtils.hasText;

import com.ject.studytrip.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Builder(access = AccessLevel.PRIVATE)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SocialProvider socialProvider;

    @Column(nullable = false)
    private String socialId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String nickname;

    private String profileImage;

    @Enumerated(EnumType.STRING)
    private MemberCategory category;

    @Enumerated(EnumType.STRING)
    private MemberRole role;

    public static Member of(
            SocialProvider socialProvider,
            String socialId,
            String email,
            String nickname,
            String profileImage,
            MemberCategory category,
            MemberRole role) {
        return Member.builder()
                .socialProvider(socialProvider)
                .socialId(socialId)
                .email(email)
                .nickname(nickname)
                .profileImage(profileImage)
                .category(category)
                .role(role)
                .build();
    }

    // 프로필 이미지 수정 로직 추가 예정
    public void update(String nickname, MemberCategory category) {
        if (hasText(nickname) && !nickname.equals(this.nickname)) { // 다른 경우에만 닉네임 수정
            this.nickname = nickname;
        }
        if (category != null && category != this.category) { // 다른 경우에만 카테고리 수정
            this.category = category;
        }
    }

    public void updateDeletedAt() {
        this.deletedAt = LocalDateTime.now();
    }
}
