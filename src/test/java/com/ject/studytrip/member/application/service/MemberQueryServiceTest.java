package com.ject.studytrip.member.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.ject.studytrip.BaseUnitTest;
import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.member.domain.error.MemberErrorCode;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.member.domain.model.MemberRole;
import com.ject.studytrip.member.domain.model.SocialProvider;
import com.ject.studytrip.member.domain.repository.MemberQueryRepository;
import com.ject.studytrip.member.domain.repository.MemberRepository;
import com.ject.studytrip.member.fixture.MemberFixture;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("MemberQueryService 단위 테스트")
class MemberQueryServiceTest extends BaseUnitTest {
    @InjectMocks private MemberQueryService memberQueryService;
    @Mock private MemberRepository memberRepository;
    @Mock private MemberQueryRepository memberQueryRepository;

    private Member member;

    private String socialId;

    @BeforeEach
    void setUp() {
        member = MemberFixture.createMemberFromKakaoWithId(1L);

        socialId = member.getSocialId();
    }

    @Nested
    @DisplayName("getMemberBySocialProviderAndSocialId 메서드는")
    class GetMemberBySocialProviderAndSocialId {

        @Test
        @DisplayName("탈퇴한 Member라면 예외가 발생한다.")
        void shouldThrowExceptionWhenMemberAlreadyDeleted() {
            // given
            member.updateDeletedAt();
            given(memberRepository.findBySocialProviderAndSocialId(SocialProvider.KAKAO, socialId))
                    .willReturn(Optional.of(member));

            // when & then
            assertThatThrownBy(
                            () ->
                                    memberQueryService.getMemberBySocialProviderAndSocialId(
                                            SocialProvider.KAKAO, socialId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MemberErrorCode.MEMBER_ALREADY_DELETED.getMessage());
        }

        @Test
        @DisplayName("소셜 ID로 조회 시 존재하면 Member를 반환한다.")
        void shouldReturnMemberWhenSocialIdExists() {
            // given
            given(memberRepository.findBySocialProviderAndSocialId(SocialProvider.KAKAO, socialId))
                    .willReturn(Optional.of(member));

            // when
            Optional<Member> result =
                    memberQueryService.getMemberBySocialProviderAndSocialId(
                            SocialProvider.KAKAO, socialId);

            // then
            assertThat(result).isEqualTo(Optional.of(member));
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
            assertThatThrownBy(() -> memberQueryService.getMember(invalidId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MemberErrorCode.MEMBER_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("멤버 ID가 존재하면 Member를 반환한다.")
        void shouldReturnMemberWhenMemberIdExists() {
            // given
            Long memberId = member.getId();
            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

            // when
            Member result = memberQueryService.getMember(memberId);

            // then
            assertThat(result).isEqualTo(member);
        }
    }

    @Nested
    @DisplayName("getValidMember 메서드는")
    class GetValidMember {

        @Test
        @DisplayName("존재하지 않는 멤버 ID로 조회하면 예외가 발생한다.")
        void shouldThrowExceptionWhenMemberIdNotFound() {
            // given
            Long invalidId = -1L;
            given(memberRepository.findByIdAndDeletedAtIsNull(invalidId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> memberQueryService.getValidMember(invalidId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MemberErrorCode.MEMBER_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("멤버가 이미 삭제된 경우 예외가 발생한다.")
        void shouldThrowExceptionWhenMemberAlreadyDeleted() {
            // given
            Long memberId = member.getId();
            member.updateDeletedAt();

            // when & then
            assertThatThrownBy(() -> memberQueryService.getValidMember(memberId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MemberErrorCode.MEMBER_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("유효한 멤버 ID가 주어지면 Member를 반환한다.")
        void shouldReturnMemberWhenMemberIdIsValid() {
            // given
            Long memberId = member.getId();
            given(memberRepository.findByIdAndDeletedAtIsNull(memberId))
                    .willReturn(Optional.of(member));

            // when
            Member result = memberQueryService.getValidMember(memberId);

            // then
            assertThat(result).isEqualTo(member);
            assertThat(result.getDeletedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("getRoleByMemberId 메서드는")
    class GetRoleByMemberId {

        @Test
        @DisplayName("존재하지 않는 멤버 ID로 조회하면 예외가 발생한다.")
        void shouldThrowExceptionWhenMemberIdNotFound() {
            // given
            String invalidId = "-1";
            given(memberQueryRepository.findMemberRoleById(Long.valueOf(invalidId)))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> memberQueryService.getRoleByMemberId(invalidId))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MemberErrorCode.MEMBER_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("유효한 멤버 ID가 주어지면 Role을 반환한다.")
        void shouldReturnRoleNameWhenMemberIdIsValid() {
            // given
            String memberId = member.getId().toString();
            MemberRole memberRole = member.getRole();
            given(memberQueryRepository.findMemberRoleById(Long.valueOf(memberId)))
                    .willReturn(Optional.of(memberRole));

            // when
            String result = memberQueryService.getRoleByMemberId(memberId);

            // then
            assertThat(result).isEqualTo(memberRole.name());
        }
    }
}
