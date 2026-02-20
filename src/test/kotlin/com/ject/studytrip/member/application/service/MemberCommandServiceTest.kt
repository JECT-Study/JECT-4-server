package com.ject.studytrip.member.application.service

import com.ject.studytrip.BaseUnitTest
import com.ject.studytrip.global.exception.CustomException
import com.ject.studytrip.global.util.EntityExtensions.requireId
import com.ject.studytrip.member.domain.error.MemberErrorCode
import com.ject.studytrip.member.domain.model.Member
import com.ject.studytrip.member.domain.model.SocialProvider
import com.ject.studytrip.member.domain.repository.MemberCommandRepository
import com.ject.studytrip.member.domain.repository.MemberRepository
import com.ject.studytrip.member.fixture.CreateMemberCommandFixture
import com.ject.studytrip.member.fixture.MemberFixture
import com.ject.studytrip.member.fixture.UpdateMemberRequestFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.kotlin.any
import org.mockito.kotlin.verify

@DisplayName("MemberCommandService 단위 테스트")
class MemberCommandServiceTest : BaseUnitTest() {
    @InjectMocks
    private lateinit var memberCommandService: MemberCommandService

    @Mock
    private lateinit var memberRepository: MemberRepository

    @Mock
    private lateinit var memberCommandRepository: MemberCommandRepository

    private lateinit var member: Member
    private lateinit var memberWithoutProfileImage: Member

    @BeforeEach
    fun setUp() {
        member = MemberFixture().createFromKakaoWithId(1L)
        memberWithoutProfileImage = MemberFixture().createFromKakaoWithoutProfileImage()
    }

    companion object {
        private const val NEW_MEMBER_NICKNAME = "새로운 멤버 닉네임"
        private const val NEW_MEMBER_CATEGORY = "WORKER"
    }

    @Nested
    @DisplayName("CreateMemberFromKakao 메서드는")
    inner class CreateMemberFromKakao {
        private val fixture = CreateMemberCommandFixture()

        @Test
        @DisplayName("회원가입된 멤버가 이미 존재하면 예외가 발생한다.")
        fun shouldThrowExceptionWhenMemberAlreadyExists() {
            // given
            val command = fixture.withNickname(member.nickname).build()
            given(memberRepository.existsBySocialProviderAndSocialId(SocialProvider.KAKAO, member.socialId)).willReturn(true)

            // when
            val exception = assertThrows<CustomException> { memberCommandService.createMemberFromKakao(command) }

            // then
            assertThat(exception.message).isEqualTo(MemberErrorCode.MEMBER_ALREADY_EXISTS.message)
        }

        @Test
        @DisplayName("CreateMemberCommand가 유효하면 멤버를 생성하고 반환한다.")
        fun shouldCreateAndReturnMemberWhenCommandIsValid() {
            // given
            val command = fixture.build()
            given(memberRepository.existsBySocialProviderAndSocialId(SocialProvider.KAKAO, member.socialId)).willReturn(false)
            given(memberRepository.save(any())).willReturn(member)

            // when
            val result = memberCommandService.createMemberFromKakao(command)

            // then
            assertThat(result).isEqualTo(member)
        }

        @Test
        @DisplayName("프로필 이미지가 존재하지 않으면 멤버를 생성하고 반환한다.")
        fun shouldCreateAndReturnMemberWhenProfileImageDoesNotExist() {
            // given
            val command = fixture.withProfileImage(null).build()
            given(
                memberRepository.existsBySocialProviderAndSocialId(SocialProvider.KAKAO, memberWithoutProfileImage.socialId),
            ).willReturn(false)
            given(memberRepository.save(any())).willReturn(memberWithoutProfileImage)

            // when
            val result = memberCommandService.createMemberFromKakao(command)

            // then
            assertThat(result).isEqualTo(memberWithoutProfileImage)
        }
    }

    @Nested
    @DisplayName("updateMember 메서드는")
    inner class UpdateMember {
        private val fixture = UpdateMemberRequestFixture()

        @Test
        @DisplayName("특정 멤버의 닉네임을 수정한다.")
        fun shouldUpdateMemberWhenNicknameIsPresent() {
            // given
            val request = fixture.withNickname(NEW_MEMBER_NICKNAME).build()

            // when
            memberCommandService.updateMember(member, request)

            // then
            assertThat(member.nickname).isEqualTo(NEW_MEMBER_NICKNAME)
        }

        @Test
        @DisplayName("특정 멤버의 카테고리를 수정한다.")
        fun shouldUpdateMemberWhenCategoryIsPresent() {
            // given
            val request = fixture.withCategory(NEW_MEMBER_CATEGORY).build()

            // when
            memberCommandService.updateMember(member, request)

            // then
            assertThat(member.category.name).isEqualTo(NEW_MEMBER_CATEGORY)
        }

        @Test
        @DisplayName("특정 멤버의 닉네임과 카테고리를 수정한다.")
        fun shouldUpdateMemberWhenNicknameAndCategoryArePresent() {
            // given
            val request = fixture.withNickname(NEW_MEMBER_NICKNAME).withCategory(NEW_MEMBER_CATEGORY).build()

            // when
            memberCommandService.updateMember(member, request)

            // then
            assertThat(member.nickname).isEqualTo(NEW_MEMBER_NICKNAME)
            assertThat(member.category.name).isEqualTo(NEW_MEMBER_CATEGORY)
        }
    }

    @Nested
    @DisplayName("updateProfileImage 메서드는")
    inner class UpdateProfileImage {
        private val newProfileImage = "https://cdn.example.com/members/1/image.jpg"

        @Test
        @DisplayName("삭제된 멤버의 프로필 이미지를 수정하면 예외가 발생한다.")
        fun shouldThrowExceptionWhenMemberIsDeleted() {
            // given
            member.updateDeletedAt()

            // when
            val exception = assertThrows<CustomException> { memberCommandService.updateProfileImage(member, newProfileImage) }

            // then
            assertThat(exception.message).isEqualTo(MemberErrorCode.MEMBER_ALREADY_DELETED.message)
        }

        @Test
        @DisplayName("유효한 멤버의 프로필 이미지를 수정한다.")
        fun shouldUpdateProfileImageWhenMemberIsValid() {
            // given
            val oldProfileImage = member.profileImage

            // when
            memberCommandService.updateProfileImage(member, newProfileImage)

            // then
            assertThat(member.profileImage).isEqualTo(newProfileImage)
            assertThat(member.profileImage).isNotEqualTo(oldProfileImage)
        }
    }

    @Nested
    @DisplayName("deleteMember 메서드는")
    inner class DeleteMember {
        @Test
        @DisplayName("멤버가 삭제될 때 deletedAt 필드를 현재 시간으로 업데이트한다. (소프트 삭제)")
        fun shouldUpdateDeletedAtWhenMemberIsDeleted() {
            // when
            memberCommandService.deleteMember(member)

            // then
            assertThat(member.deletedAt).isNotNull
        }
    }

    @Nested
    @DisplayName("restoreMember 메서드는")
    inner class RestoreMember {
        @Test
        @DisplayName("멤버가 복구될 때 deletedAt 필드를 null로 업데이트한다.")
        fun shouldRestoreDeletedAtWhenDeletedMemberIsRestored() {
            // given
            member.updateDeletedAt()

            // when
            memberCommandService.restoreMember(member)

            // then
            assertThat(member.deletedAt).isNull()
        }
    }

    @Nested
    @DisplayName("hardDeleteMembers 메서드는")
    inner class HardDeleteMembers {
        @Test
        @DisplayName("삭제된 멤버가 하나라도 존재하지 않으면 0을 반환한다.")
        fun shouldReturnZeroWhenDeletedMembersDoNotExist() {
            // given
            given(memberCommandRepository.deleteAllByDeletedAtIsNotNull()).willReturn(0L)

            // when
            val result = memberCommandService.hardDeleteMembers()

            // then
            assertThat(result).isEqualTo(0L)
        }

        @Test
        @DisplayName("삭제된 멤버가 하나라도 존재하면 해당 개수를 반환한다.")
        fun shouldReturnCountWhenDeletedMembersExist() {
            // given
            given(memberCommandRepository.deleteAllByDeletedAtIsNotNull()).willReturn(5L)

            // when
            val result = memberCommandService.hardDeleteMembers()

            // then
            assertThat(result).isEqualTo(5L)
        }
    }

    @Nested
    @DisplayName("hardDeleteMember 메서드는")
    inner class HardDeleteMember {
        @Test
        @DisplayName("특정 멤버를 완전 삭제합니다.")
        fun shouldHardDeleteMember() {
            // given
            val memberId = member.id.requireId()

            // when
            memberCommandService.hardDeleteMember(memberId)

            // then
            verify(memberRepository).deleteById(memberId)
        }
    }
}
