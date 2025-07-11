package com.ject.studytrip.member.domain.model;

import com.ject.studytrip.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
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

    @Column(nullable = false, unique = true)
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
}
