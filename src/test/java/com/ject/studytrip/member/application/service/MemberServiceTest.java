package com.ject.studytrip.member.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.ject.studytrip.BaseUnitTest;
import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.member.domain.error.MemberErrorCode;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.member.domain.model.SocialProvider;
import com.ject.studytrip.member.domain.repository.MemberRepository;
import com.ject.studytrip.member.fixture.MemberFixture;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("MemberService 단위 테스트")
class MemberServiceTest extends BaseUnitTest {
    private static final String KAKAO_ID = "12345";
    private static final String EMAIL = "choi@kakao.com";
    private static final String PROFILE_IMAGE = "https://kakao.com/profile.jpg";
    private static final String NICKNAME = "민우";
    private static final String CATEGORY = "STUDENT";

    @InjectMocks private MemberService memberService;
    @Mock private MemberRepository memberRepository;

    private Member member;
    private Member memberWithoutProfileImage;

    @BeforeEach
    void setUp() {
        member = MemberFixture.createMemberFromKakao();
        memberWithoutProfileImage = MemberFixture.createMemberWithoutProfileImageFromKakao();
    }

    @Nested
    @DisplayName("getMember 메서드는")
    class GetMember {

        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 예외가 발생한다.")
        void shouldThrowExceptionWhenMemberIdNotFound() {
            // given
            Long invalidId = -1L;
            given(memberRepository.findById(invalidId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> memberService.getMember(invalidId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MemberErrorCode.MEMBER_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("ID로 멤버를 조회하면 Member를 반환한다.")
        void shouldReturnMemberWhenMemberIdExists() {
            // given
            given(memberRepository.findById(1L)).willReturn(Optional.of(member));

            // when
            Member result = memberService.getMember(1L);

            // then
            assertThat(result).isEqualTo(member);
        }
    }

    @Nested
    @DisplayName("getMemberBySocialProviderAndSocialId 메서드는")
    class GetMemberBySocialProviderAndSocialId {

        @Test
        @DisplayName("소셜 ID로 조회 시 존재하지 않으면 예외가 발생한다.")
        void shouldThrowExceptionWhenSocialIdNotFound() {
            // given
            given(memberRepository.findBySocialProviderAndSocialId(SocialProvider.KAKAO, KAKAO_ID))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(
                            () ->
                                    memberService.getMemberBySocialProviderAndSocialId(
                                            SocialProvider.KAKAO, KAKAO_ID))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MemberErrorCode.MEMBER_NEED_SIGNUP.getMessage());
        }

        @Test
        @DisplayName("소셜 ID로 조회 시 존재하면 Member를 반환한다.")
        void shouldReturnMemberWhenSocialIdExists() {
            // given
            given(memberRepository.findBySocialProviderAndSocialId(SocialProvider.KAKAO, KAKAO_ID))
                    .willReturn(Optional.of(member));

            // when
            Member result =
                    memberService.getMemberBySocialProviderAndSocialId(
                            SocialProvider.KAKAO, KAKAO_ID);

            // then
            assertThat(result).isEqualTo(member);
        }

        @Test
        @DisplayName("이미 존재하는 멤버라면 예외가 발생한다.")
        void shouldThrowExceptionWhenMemberAlreadyExists() {
            // given
            given(
                            memberRepository.existsBySocialProviderAndSocialId(
                                    SocialProvider.KAKAO, KAKAO_ID))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(
                            () ->
                                    memberService.createMemberFromKakao(
                                            KAKAO_ID, EMAIL, PROFILE_IMAGE, CATEGORY, NICKNAME))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MemberErrorCode.MEMBER_ALREADY_EXISTS.getMessage());
        }
    }

    @Nested
    @DisplayName("createMemberFromKakao 메서드는")
    class CreateMemberFromKakao {

        @Test
        @DisplayName("카테고리가 비어 있으면 예외가 발생한다.")
        void shouldThrowExceptionWhenCategoryIsBlank() {
            // when & then
            assertThatThrownBy(
                            () ->
                                    memberService.createMemberFromKakao(
                                            KAKAO_ID, EMAIL, PROFILE_IMAGE, " ", NICKNAME))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MemberErrorCode.MEMBER_CATEGORY_REQUIRED.getMessage());
        }

        @Test
        @DisplayName("닉네임이 비어 있으면 예외가 발생한다.")
        void shouldThrowExceptionWhenNicknameIsBlank() {
            // when & then
            assertThatThrownBy(
                            () ->
                                    memberService.createMemberFromKakao(
                                            KAKAO_ID, EMAIL, PROFILE_IMAGE, CATEGORY, " "))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MemberErrorCode.MEMBER_NICKNAME_REQUIRED.getMessage());
        }

        @Test
        @DisplayName("모든 정보가 유효하면 Member를 생성하고 반환한다.")
        void shouldCreateMemberWhenAllDataIsValid() {
            // given
            given(
                            memberRepository.existsBySocialProviderAndSocialId(
                                    SocialProvider.KAKAO, KAKAO_ID))
                    .willReturn(false);
            given(memberRepository.save(any(Member.class))).willReturn(member);

            // when
            Member result =
                    memberService.createMemberFromKakao(
                            KAKAO_ID, EMAIL, PROFILE_IMAGE, CATEGORY, NICKNAME);

            // then
            assertThat(result).isEqualTo(member);
        }

        @Test
        @DisplayName("프로필 이미지가 없어도 Member를 생성하고 반환한다.")
        void shouldCreateMemberWhenProfileImageIsNull() {
            // given
            given(
                            memberRepository.existsBySocialProviderAndSocialId(
                                    SocialProvider.KAKAO, KAKAO_ID))
                    .willReturn(false);
            given(memberRepository.save(any(Member.class))).willReturn(memberWithoutProfileImage);

            // when
            Member result =
                    memberService.createMemberFromKakao(KAKAO_ID, EMAIL, null, CATEGORY, NICKNAME);

            // then
            assertThat(result).isEqualTo(memberWithoutProfileImage);
        }
    }
}
