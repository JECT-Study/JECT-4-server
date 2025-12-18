package com.ject.studytrip.member.domain.model;

import static org.springframework.util.StringUtils.hasText;

import com.ject.studytrip.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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

    public void update(String nickname, MemberCategory category) {
        if (hasText(nickname) && !nickname.equals(this.nickname)) { // 다른 경우에만 닉네임 수정
            this.nickname = nickname;
        }
        if (category != null && category != this.category) { // 다른 경우에만 카테고리 수정
            this.category = category;
        }
    }

    public void updateProfileImage(String profileImage) {
        if (hasText(profileImage)) this.profileImage = profileImage;
    }

    public void updateDeletedAt() {
        this.deletedAt = LocalDateTime.now();
    }

    public void restoreDeletedAt() {
        this.deletedAt = null;
    }

    public MemberCategory getCategory() {
        return category;
    }

    public String getEmail() {
        return email;
    }

    public Long getId() {
        return id;
    }

    public String getNickname() {
        return nickname;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public MemberRole getRole() {
        return role;
    }

    public String getSocialId() {
        return socialId;
    }

    public SocialProvider getSocialProvider() {
        return socialProvider;
    }
}
