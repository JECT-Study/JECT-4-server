package com.ject.studytrip.member.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.ject.studytrip.BaseUnitTest;
import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.member.application.dto.CreateMemberCommand;
import com.ject.studytrip.member.domain.error.MemberErrorCode;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.member.domain.model.SocialProvider;
import com.ject.studytrip.member.domain.repository.MemberRepository;
import com.ject.studytrip.member.fixture.CreateMemberCommandFixture;
import com.ject.studytrip.member.fixture.MemberFixture;
import com.ject.studytrip.member.fixture.UpdateMemberRequestFixture;
import com.ject.studytrip.member.presentation.dto.request.UpdateMemberRequest;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("MemberService 단위 테스트")
class MemberServiceTest extends BaseUnitTest {
    private static final String NEW_MEMBER_NICKNAME = "팬텀";
    private static final String NEW_MEMBER_CATEGORY = "WORKER";

    @InjectMocks private MemberService memberService;
    @Mock private MemberRepository memberRepository;

    private Member member;
    private Member memberWithoutProfileImage;

    private String socialId;
    private String nickname;
    private String category;

    @BeforeEach
    void setUp() {
        member = MemberFixture.createMemberFromKakao();
        memberWithoutProfileImage = MemberFixture.createMemberWithoutProfileImageFromKakao();

        socialId = member.getSocialId();
        nickname = member.getNickname();
        category = member.getCategory().name();
    }

    @Nested
    @DisplayName("createMemberFromKakao 메서드는")
    class CreateMemberFromKakao {
        private final CreateMemberCommandFixture fixture = new CreateMemberCommandFixture();

        @Test
        @DisplayName("이미 가입된 멤버가 존재하면 예외가 발생한다.")
        void shouldThrowExceptionWhenMemberAlreadyExists() {
            // given
            CreateMemberCommand command = fixture.withNickname(nickname).build();
            given(
                            memberRepository.existsBySocialProviderAndSocialId(
                                    SocialProvider.KAKAO, socialId))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() -> memberService.createMemberFromKakao(command))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MemberErrorCode.MEMBER_ALREADY_EXISTS.getMessage());
        }

        @Test
        @DisplayName("카테고리가 유효하지 않으면 예외가 발생한다.")
        void shouldThrowExceptionWhenCategoryIsInValid() {
            // given
            CreateMemberCommand command = fixture.withCategory("INVALID").build();
            given(
                            memberRepository.existsBySocialProviderAndSocialId(
                                    SocialProvider.KAKAO, socialId))
                    .willReturn(false);

            // when & then
            assertThatThrownBy(() -> memberService.createMemberFromKakao(command))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MemberErrorCode.INVALID_MEMBER_CATEGORY.getMessage());
        }

        @Test
        @DisplayName("CreateMemberCommand가 유효하면 Member를 생성하고 반환한다.")
        void shouldReturnMemberWhenCommandIsValid() {
            // given
            CreateMemberCommand command = fixture.build();
            given(
                            memberRepository.existsBySocialProviderAndSocialId(
                                    SocialProvider.KAKAO, socialId))
                    .willReturn(false);
            given(memberRepository.save(any(Member.class))).willReturn(member);

            // when
            Member result = memberService.createMemberFromKakao(command);

            // then
            assertThat(result).isEqualTo(member);
        }

        @Test
        @DisplayName("프로필 이미지가 없어도 Member를 생성하고 반환한다.")
        void shouldReturnMemberWhenProfileImageIsNull() {
            // given
            CreateMemberCommand command = fixture.withProfileImage(null).build();
            given(
                            memberRepository.existsBySocialProviderAndSocialId(
                                    SocialProvider.KAKAO, socialId))
                    .willReturn(false);
            given(memberRepository.save(any(Member.class))).willReturn(memberWithoutProfileImage);

            // when
            Member result = memberService.createMemberFromKakao(command);

            // then
            assertThat(result).isEqualTo(memberWithoutProfileImage);
        }
    }

    @Nested
    @DisplayName("updateNicknameAndCategoryIfPresent 메서드는")
    class UpdateNicknameAndCategoryIfPresent {
        private final UpdateMemberRequestFixture fixture = new UpdateMemberRequestFixture();

        @Test
        @DisplayName("카테고리가 유효하지 않으면 예외가 발생한다.")
        void shouldThrowExceptionWhenCategoryIsInValid() {
            // given
            UpdateMemberRequest request = fixture.withCategory("INVALID").build();

            // when & then
            assertThatThrownBy(
                            () -> memberService.updateNicknameAndCategoryIfPresent(member, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MemberErrorCode.INVALID_MEMBER_CATEGORY.getMessage());
        }

        @Test
        @DisplayName("특정 멤버의 닉네임만 수정하고 DB에 반영한다.")
        void shouldUpdateMemberNickname() {
            // given
            UpdateMemberRequest request = fixture.withNickname(NEW_MEMBER_NICKNAME).build();

            // when
            memberService.updateNicknameAndCategoryIfPresent(member, request);

            // then
            assertThat(member.getNickname()).isEqualTo(NEW_MEMBER_NICKNAME);
            assertThat(member.getCategory().name()).isEqualTo(category);
        }

        @Test
        @DisplayName("특정 멤버의 카테고리만 수정하고 DB에 반영한다.")
        void shouldUpdateMemberCategory() {
            // given
            UpdateMemberRequest request = fixture.withCategory(NEW_MEMBER_CATEGORY).build();

            // when
            memberService.updateNicknameAndCategoryIfPresent(member, request);

            // then
            assertThat(member.getNickname()).isEqualTo(nickname);
            assertThat(member.getCategory().name()).isEqualTo(NEW_MEMBER_CATEGORY);
        }

        @Test
        @DisplayName("특정 멤버의 카테고리만 수정하고 DB에 반영한다.")
        void shouldUpdateMemberNicknameAndCategory() {
            // given
            UpdateMemberRequest request =
                    fixture.withNickname(NEW_MEMBER_NICKNAME)
                            .withCategory(NEW_MEMBER_CATEGORY)
                            .build();

            // when
            memberService.updateNicknameAndCategoryIfPresent(member, request);

            // then
            assertThat(member.getNickname()).isEqualTo(NEW_MEMBER_NICKNAME);
            assertThat(member.getCategory().name()).isEqualTo(NEW_MEMBER_CATEGORY);
        }
    }

    @Nested
    @DisplayName("deleteMember 메서드는")
    class DeleteMember {

        //        @Test
        //        @DisplayName("멤버가 이미 삭제된 경우 예외가 발생한다.")
        //        void shouldThrowExceptionWhenMemberAlreadyDeleted() {
        //            // given
        //            ReflectionTestUtils.setField(member, "deletedAt", LocalDateTime.now());
        //
        //            // when & then
        //            assertThatThrownBy(() -> memberService.deleteMember(member))
        //                    .isInstanceOf(CustomException.class)
        //                    .hasMessage(MemberErrorCode.MEMBER_ALREADY_DELETED.getMessage());
        //        }

        @Test
        @DisplayName("멤버를 삭제하면 deletedAt 필드에 현재 시각이 설정된다.")
        void shouldDeleteMember() {
            // given
            assertThat(member.getDeletedAt()).isNull();
            LocalDateTime beforeDeletionTime = LocalDateTime.now();

            // when
            memberService.deleteMember(member);

            // then
            assertThat(member.getDeletedAt()).isNotNull();
            assertThat(member.getDeletedAt()).isAfterOrEqualTo(beforeDeletionTime);
        }
    }

    @Nested
    @DisplayName("getMember 메서드는")
    class GetMember {

        @Test
        @DisplayName("존재하지 않는 멤버 ID로 조회하면 예외가 발생한다.")
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
        @DisplayName("멤버 ID가 존재하면 Member를 반환한다.")
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
            given(memberRepository.findBySocialProviderAndSocialId(SocialProvider.KAKAO, socialId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(
                            () ->
                                    memberService.getMemberBySocialProviderAndSocialId(
                                            SocialProvider.KAKAO, socialId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MemberErrorCode.MEMBER_NEED_SIGNUP.getMessage());
        }

        @Test
        @DisplayName("소셜 ID로 조회 시 존재하면 Member를 반환한다.")
        void shouldReturnMemberWhenSocialIdExists() {
            // given
            given(memberRepository.findBySocialProviderAndSocialId(SocialProvider.KAKAO, socialId))
                    .willReturn(Optional.of(member));

            // when
            Member result =
                    memberService.getMemberBySocialProviderAndSocialId(
                            SocialProvider.KAKAO, socialId);

            // then
            assertThat(result).isEqualTo(member);
        }
    }

    @Nested
    @DisplayName("getActiveMemberById 메서드는")
    class GetActiveMemberById {

        @Test
        @DisplayName("존재하지 않는 멤버 ID로 조회하면 예외가 발생한다.")
        void shouldThrowExceptionWhenMemberIdNotFound() {
            // given
            Long invalidId = -1L;
            given(memberRepository.findByIdAndDeletedAtIsNull(invalidId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> memberService.getActiveMemberById(invalidId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MemberErrorCode.MEMBER_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("멤버가 이미 삭제된 경우 예외가 발생한다.")
        void shouldThrowExceptionWhenMemberAlreadyDeleted() {
            // given
            Long memberId = member.getId();
            ReflectionTestUtils.setField(member, "deletedAt", LocalDateTime.now());

            // when & then
            assertThatThrownBy(() -> memberService.getActiveMemberById(memberId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MemberErrorCode.MEMBER_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("유효한 ID가 주어지면 Member를 반환한다.")
        void shouldReturnMemberWhenIdIsValid() {
            // given
            Long memberId = member.getId();
            given(memberRepository.findByIdAndDeletedAtIsNull(memberId))
                    .willReturn(Optional.of(member));

            // when
            Member result = memberService.getActiveMemberById(memberId);

            // then
            assertThat(result).isEqualTo(member);
            assertThat(result.getDeletedAt()).isNull();
        }
    }
}
