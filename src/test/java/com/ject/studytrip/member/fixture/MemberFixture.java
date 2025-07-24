package com.ject.studytrip.member.fixture;

import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.member.domain.model.MemberCategory;
import com.ject.studytrip.member.factory.MemberFactory;
import org.springframework.test.util.ReflectionTestUtils;

public class MemberFixture {
    private static final String KAKAO_ID = "12345";
    private static final String EMAIL = "choi@kakao.com";
    private static final String PROFILE_IMAGE = "https://kakao.com/profile.jpg";
    private static final String MEMBER_NICKNAME = "민우";
    private static final MemberCategory MEMBER_CATEGORY = MemberCategory.STUDENT;

    public static Member createMemberFromKakao() {
        return MemberFactory.createFromKakao(
                KAKAO_ID, EMAIL, PROFILE_IMAGE, MEMBER_NICKNAME, MEMBER_CATEGORY);
    }

    public static Member createMemberFromKakao(String email, String nickname) {
        return MemberFactory.createFromKakao(
                KAKAO_ID, email, PROFILE_IMAGE, nickname, MEMBER_CATEGORY);
    }

    public static Member createMemberFromKakaoWithId(Long id) {
        Member member =
                MemberFactory.createFromKakao(
                        KAKAO_ID, EMAIL, PROFILE_IMAGE, MEMBER_NICKNAME, MEMBER_CATEGORY);
        ReflectionTestUtils.setField(member, "id", id);

        return member;
    }

    public static Member createMemberWithoutProfileImageFromKakao() {
        return MemberFactory.createFromKakao(
                KAKAO_ID, EMAIL, null, MEMBER_NICKNAME, MEMBER_CATEGORY);
    }
}
