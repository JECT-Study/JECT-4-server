package com.ject.studytrip.member.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.ject.studytrip.BaseUnitTest;
import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.member.application.dto.CreateMemberCommand;
import com.ject.studytrip.member.domain.error.MemberErrorCode;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.member.domain.model.SocialProvider;
import com.ject.studytrip.member.domain.repository.MemberCommandRepository;
import com.ject.studytrip.member.domain.repository.MemberRepository;
import com.ject.studytrip.member.fixture.CreateMemberCommandFixture;
import com.ject.studytrip.member.fixture.MemberFixture;
import com.ject.studytrip.member.fixture.UpdateMemberRequestFixture;
import com.ject.studytrip.member.presentation.dto.request.UpdateMemberRequest;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("MemberCommandService 단위 테스트")
class MemberCommandServiceTest extends BaseUnitTest {
    private static final String NEW_MEMBER_NICKNAME = "팬텀";
    private static final String NEW_MEMBER_CATEGORY = "WORKER";

    @InjectMocks private MemberCommandService memberCommandService;
    @Mock private MemberRepository memberRepository;
    @Mock private MemberCommandRepository memberCommandRepository;

    private Member member;
    private Member memberWithoutProfileImage;

    private String socialId;
    private String nickname;
    private String category;

    @BeforeEach
    void setUp() {
        member = MemberFixture.createMemberFromKakaoWithId(1L);
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
            assertThatThrownBy(() -> memberCommandService.createMemberFromKakao(command))
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
            assertThatThrownBy(() -> memberCommandService.createMemberFromKakao(command))
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
            Member result = memberCommandService.createMemberFromKakao(command);

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
            Member result = memberCommandService.createMemberFromKakao(command);

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
                            () ->
                                    memberCommandService.updateNicknameAndCategoryIfPresent(
                                            member, request))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MemberErrorCode.INVALID_MEMBER_CATEGORY.getMessage());
        }

        @Test
        @DisplayName("특정 멤버의 닉네임만 수정하고 DB에 반영한다.")
        void shouldUpdateMemberNickname() {
            // given
            UpdateMemberRequest request = fixture.withNickname(NEW_MEMBER_NICKNAME).build();

            // when
            memberCommandService.updateNicknameAndCategoryIfPresent(member, request);

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
            memberCommandService.updateNicknameAndCategoryIfPresent(member, request);

            // then
            assertThat(member.getNickname()).isEqualTo(nickname);
            assertThat(member.getCategory().name()).isEqualTo(NEW_MEMBER_CATEGORY);
        }

        @Test
        @DisplayName("특정 멤버의 닉네임과 카테고리를 수정하고 DB에 반영한다.")
        void shouldUpdateMemberNicknameAndCategory() {
            // given
            UpdateMemberRequest request =
                    fixture.withNickname(NEW_MEMBER_NICKNAME)
                            .withCategory(NEW_MEMBER_CATEGORY)
                            .build();

            // when
            memberCommandService.updateNicknameAndCategoryIfPresent(member, request);

            // then
            assertThat(member.getNickname()).isEqualTo(NEW_MEMBER_NICKNAME);
            assertThat(member.getCategory().name()).isEqualTo(NEW_MEMBER_CATEGORY);
        }
    }

    @Nested
    @DisplayName("updateProfileImage 메서드는")
    class UpdateProfileImage {
        private static final String NEW_PROFILE_IMAGE =
                "https://cdn.example.com/members/1/profile.jpg";

        @Test
        @DisplayName("삭제된 멤버의 프로필 이미지를 수정하면 예외가 발생한다")
        void shouldThrowExceptionWhenMemberIsDeleted() {
            // given
            member.updateDeletedAt();

            // when & then
            assertThatThrownBy(
                            () ->
                                    memberCommandService.updateProfileImage(
                                            member, NEW_PROFILE_IMAGE))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(MemberErrorCode.MEMBER_ALREADY_DELETED.getMessage());
        }

        @Test
        @DisplayName("유효한 멤버의 프로필 이미지를 수정한다")
        void shouldUpdateProfileImageWhenMemberIsValid() {
            // given
            String oldProfileImage = member.getProfileImage();

            // when
            memberCommandService.updateProfileImage(member, NEW_PROFILE_IMAGE);

            // then
            assertThat(member.getProfileImage()).isEqualTo(NEW_PROFILE_IMAGE);
            assertThat(member.getProfileImage()).isNotEqualTo(oldProfileImage);
        }
    }

    @Nested
    @DisplayName("deleteMember 메서드는")
    class DeleteMember {

        @Test
        @DisplayName("멤버를 삭제하면 deletedAt 필드에 현재 시각이 설정된다.")
        void shouldDeleteMember() {
            // given
            assertThat(member.getDeletedAt()).isNull();
            LocalDateTime beforeDeletionTime = LocalDateTime.now();

            // when
            memberCommandService.deleteMember(member);

            // then
            assertThat(member.getDeletedAt()).isNotNull();
            assertThat(member.getDeletedAt()).isAfterOrEqualTo(beforeDeletionTime);
        }
    }

    @Nested
    @DisplayName("restoreMember 메서드는")
    class RestoreMember {

        @Test
        @DisplayName("삭제된 멤버가 복구될 때 deletedAt 필드를 null로 업데이트한다.")
        void shouldRestoreDeletedAtWhenDeletedMemberIsRestored() {
            // given
            member.updateDeletedAt();

            // when
            memberCommandService.restoreMember(member);

            // then
            assertThat(member.getDeletedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("hardDeleteMembers 메서드는")
    class HardDeleteMembers {

        @Test
        @DisplayName("삭제된 멤버가 없으면 0을 반환한다.")
        void shouldReturnZeroWhenDeletedMembersDoNotExist() {
            // given
            given(memberCommandRepository.deleteAllByDeletedAtIsNotNull()).willReturn(0L);

            // when
            long result = memberCommandService.hardDeleteMembers();

            // then
            assertThat(result).isEqualTo(0L);
        }

        @Test
        @DisplayName("삭제된 멤버가 있으면 해당 개수를 반환한다.")
        void shouldReturnCountWhenDeletedMembersExist() {
            // given
            given(memberCommandRepository.deleteAllByDeletedAtIsNotNull()).willReturn(5L);

            // when
            long result = memberCommandService.hardDeleteMembers();

            // then
            assertThat(result).isEqualTo(5L);
        }
    }

    @Nested
    @DisplayName("hardDeleteMemberById 메서드는")
    class HardDeleteMemberById {

        @Test
        @DisplayName("전달된 멤버 ID로 삭제를 수행한다")
        void shouldDeleteById() {
            // given
            Long memberId = 123L;

            // when
            memberCommandService.hardDeleteMemberById(memberId);

            // then
            verify(memberRepository).deleteById(memberId);
        }
    }
}
