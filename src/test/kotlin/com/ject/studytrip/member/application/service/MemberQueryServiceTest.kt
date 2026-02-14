package com.ject.studytrip.member.application.service

import com.ject.studytrip.BaseUnitTest
import com.ject.studytrip.global.exception.CustomException
import com.ject.studytrip.member.domain.error.MemberErrorCode
import com.ject.studytrip.member.domain.model.Member
import com.ject.studytrip.member.domain.model.SocialProvider
import com.ject.studytrip.member.domain.repository.MemberQueryRepository
import com.ject.studytrip.member.domain.repository.MemberRepository
import com.ject.studytrip.member.fixture.MemberFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import java.util.Optional

@DisplayName("MemberQueryService 단위 테스트")
class MemberQueryServiceTest : BaseUnitTest() {
    @InjectMocks
    private lateinit var memberQueryService: MemberQueryService

    @Mock
    private lateinit var memberRepository: MemberRepository

    @Mock
    private lateinit var memberQueryRepository: MemberQueryRepository

    private lateinit var member: Member
    private lateinit var socialId: String

    @BeforeEach
    fun setUp() {
        member = MemberFixture().createFromKakaoWithId(1L)
        socialId = member.socialId
    }

    @Nested
    @DisplayName("getMemberBySocialProviderAndSocialId 메서드는")
    inner class GetMemberBySocialProviderAndSocialId {
        @Test
        @DisplayName("멤버가 이미 삭제되었다면 예외가 발생한다.")
        fun shouldThrowExceptionWhenMemberAlreadyDeleted() {
            // given
            member.updateDeletedAt()
            given(memberRepository.findBySocialProviderAndSocialId(SocialProvider.KAKAO, socialId)).willReturn(Optional.of(member))

            // when
            val exception =
                assertThrows<CustomException> { memberQueryService.getMemberBySocialProviderAndSocialId(SocialProvider.KAKAO, socialId) }

            // then
            assertThat(exception.message).isEqualTo(MemberErrorCode.MEMBER_ALREADY_DELETED.message)
        }

        @Test
        @DisplayName("소셜 ID에 대한 멤버가 존재하면 멤버를 반환한다.")
        fun shouldReturnMemberWhenSocialIdExists() {
            // given
            given(memberRepository.findBySocialProviderAndSocialId(SocialProvider.KAKAO, socialId)).willReturn(Optional.of(member))

            // when
            val result = memberQueryService.getMemberBySocialProviderAndSocialId(SocialProvider.KAKAO, socialId)

            // then
            assertThat(result).isEqualTo(Optional.of(member))
        }
    }

    @Nested
    @DisplayName("getValidMember 메서드는")
    inner class GetValidMember {
        @Test
        @DisplayName("멤버가 존재하지 않으면 예외가 발생한다.")
        fun shouldThrowExceptionWhenMemberDoesNotExist() {
            // given
            val memberId = -1L
            given(memberRepository.findById(memberId)).willReturn(Optional.empty())

            // when
            val exception = assertThrows<CustomException> { memberQueryService.getValidMember(memberId) }

            // then
            assertThat(exception.message).isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND.message)
        }

        @Test
        @DisplayName("멤버가 이미 삭제되었다면 예외가 발생한다.")
        fun shouldThrowExceptionWhenMemberAlreadyDeleted() {
            // given
            val memberId = member.id
            member.updateDeletedAt()
            given(memberRepository.findById(memberId)).willReturn(Optional.of(member))

            // when
            val exception = assertThrows<CustomException> { memberQueryService.getValidMember(memberId) }

            // then
            assertThat(exception.message).isEqualTo(MemberErrorCode.MEMBER_ALREADY_DELETED.message)
        }

        @Test
        @DisplayName("멤버가 존재하면 멤버를 반환한다.")
        fun shouldReturnTripWhenMemberExists() {
            // given
            val memberId = member.id
            given(memberRepository.findById(memberId)).willReturn(Optional.of(member))

            // when
            val result = memberQueryService.getValidMember(memberId)

            // then
            assertThat(result).isEqualTo(member)
        }
    }

    @Nested
    @DisplayName("getDeletedMember 메서드는")
    inner class GetDeletedMember {
        @Test
        @DisplayName("멤버가 존재하지 않으면 예외가 발생한다.")
        fun shouldThrowExceptionWhenMemberDoesNotExist() {
            // given
            val memberId = -1L
            given(memberRepository.findById(memberId)).willReturn(Optional.empty())

            // when
            val exception = assertThrows<CustomException> { memberQueryService.getDeletedMember(memberId) }

            // then
            assertThat(exception.message).isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND.message)
        }

        @Test
        @DisplayName("멤버가 삭제되지 않았다면 예외가 발생한다.")
        fun shouldThrowExceptionWhenMemberIsNotDeleted() {
            // given
            val memberId = member.id
            given(memberRepository.findById(memberId)).willReturn(Optional.of(member))

            // when
            val exception = assertThrows<CustomException> { memberQueryService.getDeletedMember(memberId) }

            // then
            assertThat(exception.message).isEqualTo(MemberErrorCode.MEMBER_NOT_DELETED.message)
        }

        @Test
        @DisplayName("멤버가 이미 삭제되었다면 멤버를 반환한다.")
        fun shouldReturnMemberWhenMemberAlreadyDeleted() {
            // given
            val memberId = member.id
            member.updateDeletedAt()
            given(memberRepository.findById(memberId)).willReturn(Optional.of(member))

            // when
            val result = memberQueryService.getDeletedMember(memberId)

            // then
            assertThat(result).isEqualTo(member)
        }
    }

    @Nested
    @DisplayName("getMemberRoleByMemberId 메서드는")
    inner class GetMemberRoleByMemberId {
        @Test
        @DisplayName("멤버가 존재하지 않으면 예외가 발생한다.")
        fun shouldThrowExceptionWhenMemberDoesNotExist() {
            // given
            val memberId = -1L
            given(memberQueryRepository.findMemberRoleById(memberId)).willReturn(Optional.empty())

            // when
            val exception = assertThrows<CustomException> { memberQueryService.getMemberRoleByMemberId(memberId) }

            // then
            assertThat(exception.message).isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND.message)
        }

        @Test
        @DisplayName("멤버 ID에 대한 멤버가 존재하면 MemberRole을 반환한다.")
        fun shouldReturnMemberRoleWhenMemberIdExists() {
            // given
            val memberId = member.id
            val memberRole = member.role
            given(memberQueryRepository.findMemberRoleById(memberId.toLong())).willReturn(Optional.of(memberRole))

            // when
            val result = memberQueryService.getMemberRoleByMemberId(memberId)

            // then
            assertThat(result).isEqualTo(memberRole)
        }
    }
}
