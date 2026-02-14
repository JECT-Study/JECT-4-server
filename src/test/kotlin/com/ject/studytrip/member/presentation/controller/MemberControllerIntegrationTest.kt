package com.ject.studytrip.member.presentation.controller

import com.ject.studytrip.BaseIntegrationTest
import com.ject.studytrip.auth.domain.error.AuthErrorCode
import com.ject.studytrip.auth.fixture.TokenFixture
import com.ject.studytrip.auth.helper.TokenTestHelper
import com.ject.studytrip.global.exception.error.CommonErrorCode
import com.ject.studytrip.image.domain.error.ImageErrorCode
import com.ject.studytrip.image.infra.s3.provider.S3ImageStorageProvider
import com.ject.studytrip.member.domain.error.MemberErrorCode
import com.ject.studytrip.member.domain.model.Member
import com.ject.studytrip.member.domain.model.MemberRole
import com.ject.studytrip.member.fixture.ConfirmProfileImageRequestFixture
import com.ject.studytrip.member.fixture.PresignProfileImageRequestFixture
import com.ject.studytrip.member.fixture.UpdateMemberRequestFixture
import com.ject.studytrip.member.helper.MemberTestHelper
import com.ject.studytrip.member.presentation.dto.request.ConfirmProfileImageRequest
import com.ject.studytrip.member.presentation.dto.request.PresignProfileImageRequest
import com.ject.studytrip.member.presentation.dto.request.UpdateMemberRequest
import com.ject.studytrip.trip.helper.TripTestHelper
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@DisplayName("MemberController 통합 테스트")
class MemberControllerIntegrationTest : BaseIntegrationTest() {
    @Autowired
    private lateinit var memberTestHelper: MemberTestHelper

    @Autowired
    private lateinit var tokenTestHelper: TokenTestHelper

    @Autowired
    private lateinit var tripTestHelper: TripTestHelper

    @MockitoBean lateinit var s3ImageStorageProvider: S3ImageStorageProvider

    private lateinit var member: Member
    private lateinit var token: String
    private lateinit var deletedMember: Member
    private lateinit var deletedToken: String

    @BeforeEach
    fun setUp() {
        member = memberTestHelper.saveMember()
        token = tokenTestHelper.createAccessToken(member.id.toString(), MemberRole.ROLE_USER.name)

        // 삭제된 멤버
        deletedMember = memberTestHelper.saveDeletedMember("test@gmail.com", "WORKER")
        deletedToken = tokenTestHelper.createAccessToken(deletedMember.id.toString(), MemberRole.ROLE_USER.name)
    }

    companion object {
        private const val BASE_MEMBER_URL = "/api/members"
    }

    @Nested
    @DisplayName("멤버 수정 API")
    inner class UpdateMember {
        private val fixture = UpdateMemberRequestFixture()

        private fun getResultActions(
            token: String,
            request: UpdateMemberRequest,
        ): ResultActions =
            mockMvc.perform(
                patch("$BASE_MEMBER_URL/me")
                    .header(HttpHeaders.AUTHORIZATION, TokenFixture().authorization(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            )

        @Test
        @DisplayName("인증되지 않은 사용자라면 401 Unauthorized를 반환한다.")
        fun shouldReturnUnauthorizedWhenUnauthenticated() {
            // given
            val request = fixture.build()

            // when
            val resultActions = getResultActions("", request)

            // then
            resultActions
                .andExpect(status().isUnauthorized)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(AuthErrorCode.UNAUTHENTICATED.status.value()))
                .andExpect(jsonPath("$.data.message").value(AuthErrorCode.UNAUTHENTICATED.message))
        }

        @Test
        @DisplayName("UpdateMemberRequest 닉네임이 유효하지 않으면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenRequestNicknameIsInvalid() {
            // given
            val request = fixture.withNickname("!").build()

            // when
            val resultActions = getResultActions(token, request)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(CommonErrorCode.METHOD_ARGUMENT_NOT_VALID.status.value()))
                .andExpect(jsonPath("$.data.message").value(CommonErrorCode.METHOD_ARGUMENT_NOT_VALID.message))
        }

        @Test
        @DisplayName("UpdateMemberRequest 카테고리가 유효하지 않으면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenRequestCategoryIsInvalid() {
            // given
            val request = fixture.withCategory("INVALID").build()

            // when
            val resultActions = getResultActions(token, request)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(CommonErrorCode.METHOD_ARGUMENT_NOT_VALID.status.value()))
                .andExpect(jsonPath("$.data.message").value(CommonErrorCode.METHOD_ARGUMENT_NOT_VALID.message))
        }

        @Test
        @DisplayName("멤버가 이미 삭제되었다면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenMemberAlreadyDeleted() {
            // given
            val request = fixture.build()

            // when
            val resultActions = getResultActions(deletedToken, request)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(MemberErrorCode.MEMBER_ALREADY_DELETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(MemberErrorCode.MEMBER_ALREADY_DELETED.message))
        }

        @Test
        @DisplayName("유효한 요청이 들어오면 멤버를 수정한다.")
        fun shouldUpdateMemberNicknameWhenRequestIsValid() {
            // given
            val request = fixture.withNickname("팬텀").withCategory("JOBSEEKER").build()

            // when
            val resultActions = getResultActions(token, request)

            // then
            resultActions
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
        }
    }

    @Nested
    @DisplayName("멤버 삭제 API")
    inner class DeleteMember {
        private fun getResultActions(token: String): ResultActions =
            mockMvc.perform(
                delete("$BASE_MEMBER_URL/me")
                    .header(HttpHeaders.AUTHORIZATION, TokenFixture().authorization(token)),
            )

        @Test
        @DisplayName("인증되지 않은 사용자라면 401 Unauthorized를 반환한다.")
        fun shouldReturnUnauthorizedWhenUnauthenticated() {
            // when
            val resultActions = getResultActions("")

            // then
            resultActions
                .andExpect(status().isUnauthorized)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(AuthErrorCode.UNAUTHENTICATED.status.value()))
                .andExpect(jsonPath("$.data.message").value(AuthErrorCode.UNAUTHENTICATED.message))
        }

        @Test
        @DisplayName("멤버가 이미 삭제되었다면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenMemberAlreadyDeleted() {
            // when
            val resultActions = getResultActions(deletedToken)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(MemberErrorCode.MEMBER_ALREADY_DELETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(MemberErrorCode.MEMBER_ALREADY_DELETED.message))
        }

        @Test
        @DisplayName("특정 멤버를 삭제한다.")
        fun shouldDeleteMember() {
            // when
            val resultActions = getResultActions(token)

            // then
            resultActions
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
        }
    }

    @Nested
    @DisplayName("멤버 복구 API")
    inner class RestoreMember {
        private fun getResultActions(memberId: Any): ResultActions =
            mockMvc.perform(
                patch("$BASE_MEMBER_URL/me/restore/{memberId}", memberId)
                    .header(HttpHeaders.AUTHORIZATION, TokenFixture().authorization(token))
                    .contentType(MediaType.APPLICATION_JSON),
            )

        @Test
        @DisplayName("PathVariable 멤버 ID 타입이 올바르지 않으면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenMemberIdTypeMismatch() {
            // given
            val memberId = "abc"

            // when
            val resultActions = getResultActions(memberId)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH.status.value()))
                .andExpect(jsonPath("$.data.message").value(CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH.message))
        }

        @Test
        @DisplayName("멤버가 존재하지 않으면 404 NotFound를 반환한다.")
        fun shouldReturnNotFoundWhenMemberDoesNotExist() {
            // given
            val memberId = -1L

            // when
            val resultActions = getResultActions(memberId)

            // then
            resultActions
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(MemberErrorCode.MEMBER_NOT_FOUND.status.value()))
                .andExpect(jsonPath("$.data.message").value(MemberErrorCode.MEMBER_NOT_FOUND.message))
        }

        @Test
        @DisplayName("멤버가 삭제되지 않았다면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenMemberIsNotDeleted() {
            // given
            val memberId = member.id

            // when
            val resultActions = getResultActions(memberId)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(MemberErrorCode.MEMBER_NOT_DELETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(MemberErrorCode.MEMBER_NOT_DELETED.message))
        }

        @Test
        @DisplayName("삭제된 멤버를 복구한다.")
        fun shouldRestoreMember() {
            // given
            val memberId = deletedMember.id

            // when
            val resultActions = getResultActions(memberId)

            // then
            resultActions
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
        }
    }

    @Nested
    @DisplayName("멤버 즉시 삭제 API")
    inner class HardDeleteMember {
        private fun getResultActions(token: String): ResultActions =
            mockMvc.perform(
                delete("$BASE_MEMBER_URL/me/hard-delete")
                    .header(HttpHeaders.AUTHORIZATION, TokenFixture().authorization(token)),
            )

        @Test
        @DisplayName("인증되지 않은 사용자라면 401 Unauthorized를 반환한다.")
        fun shouldReturnUnauthorizedWhenUnauthenticated() {
            // when
            val resultActions = getResultActions("")

            // then
            resultActions
                .andExpect(status().isUnauthorized)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(AuthErrorCode.UNAUTHENTICATED.status.value()))
                .andExpect(jsonPath("$.data.message").value(AuthErrorCode.UNAUTHENTICATED.message))
        }

        @Test
        @DisplayName("멤버가 이미 삭제되었다면 400 Bad Request를 반환한다. (배치 삭제 대상)")
        fun shouldReturnBadRequestWhenMemberAlreadyDeleted() {
            // when
            val resultActions = getResultActions(deletedToken)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(MemberErrorCode.MEMBER_ALREADY_DELETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(MemberErrorCode.MEMBER_ALREADY_DELETED.message))
        }

        @Test
        @DisplayName("특정 멤버를 즉시 삭제하고, 관련된 모든 데이터를 즉시 삭제한다. (CASCADE)")
        fun shouldHardDeleteMember() {
            // when
            val resultActions = getResultActions(token)

            // then
            resultActions
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
        }
    }

    @Nested
    @DisplayName("멤버 프로필 이미지 Presigned URL 발급 API")
    inner class IssuePresignedUrl {
        private val fixture = PresignProfileImageRequestFixture()

        private fun getResultActions(
            token: String,
            request: PresignProfileImageRequest,
        ): ResultActions =
            mockMvc.perform(
                post("$BASE_MEMBER_URL/profile-images/presigned")
                    .header(HttpHeaders.AUTHORIZATION, TokenFixture().authorization(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            )

        @Test
        @DisplayName("인증되지 않은 사용자라면 401 Unauthorized를 반환한다.")
        fun shouldReturnUnauthorizedWhenUnauthenticated() {
            // given
            val request = fixture.build()

            // when
            val resultActions = getResultActions("", request)

            // then
            resultActions
                .andExpect(status().isUnauthorized)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(AuthErrorCode.UNAUTHENTICATED.status.value()))
                .andExpect(jsonPath("$.data.message").value(AuthErrorCode.UNAUTHENTICATED.message))
        }

        @Test
        @DisplayName("파일명이 비어있으면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenFilenameIsEmpty() {
            // given
            val request = fixture.withOriginFilename("").build()

            // when
            val resultActions = getResultActions(token, request)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(CommonErrorCode.METHOD_ARGUMENT_NOT_VALID.status.value()))
                .andExpect(jsonPath("$.data.message").value(CommonErrorCode.METHOD_ARGUMENT_NOT_VALID.message))
        }

        @Test
        @DisplayName("유효하지 않은 확장자는 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenExtensionIsInvalid() {
            // given
            val request = fixture.withOriginFilename("test.pdf").build()

            // when
            val resultActions = getResultActions(token, request)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(ImageErrorCode.INVALID_IMAGE_EXTENSION.status.value()))
                .andExpect(jsonPath("$.data.message").value(ImageErrorCode.INVALID_IMAGE_EXTENSION.message))
        }

        @Test
        @DisplayName("파일명이 유효하면 Presigned URL을 발급한다.")
        fun shouldIssuePresignedUrlWhenFilenameIsValid() {
            // given
            val request = fixture.build()
            given(s3ImageStorageProvider.issuePresignedUrl(anyString())).willReturn("https://mocked-presigned-url.com")

            // when
            val resultActions = getResultActions(token, request)

            // then
            resultActions
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.presignedUrl").isNotEmpty)
                .andExpect(jsonPath("$.data.tmpKey").isNotEmpty)
                .andExpect(jsonPath("$.data.tmpKey").value(Matchers.startsWith("tmp/members/")))

            // S3Provider 호출 검증
            verify(s3ImageStorageProvider).issuePresignedUrl(anyString())
        }
    }

    @Nested
    @DisplayName("멤버 프로필 이미지 확정 API")
    inner class ConfirmProfileImage {
        private val fixture = ConfirmProfileImageRequestFixture()

        private fun getResultActions(
            token: String,
            request: ConfirmProfileImageRequest,
        ): ResultActions =
            mockMvc.perform(
                post("$BASE_MEMBER_URL/profile-images/confirm")
                    .header(HttpHeaders.AUTHORIZATION, TokenFixture().authorization(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)),
            )

        @Test
        @DisplayName("인증되지 않은 사용자라면 401 Unauthorized를 반환한다.")
        fun shouldReturnUnauthorizedWhenUnauthenticated() {
            // given
            val request = fixture.build()

            // when
            val resultActions = getResultActions("", request)

            // then
            resultActions
                .andExpect(status().isUnauthorized)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(AuthErrorCode.UNAUTHENTICATED.status.value()))
                .andExpect(jsonPath("$.data.message").value(AuthErrorCode.UNAUTHENTICATED.message))
        }

        @Test
        @DisplayName("tmpKey가 비어있으면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenTmpKeyIsEmpty() {
            // given
            val request = fixture.withTmpKey("").build()

            // when
            val resultActions = getResultActions(token, request)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(CommonErrorCode.METHOD_ARGUMENT_NOT_VALID.status.value()))
                .andExpect(jsonPath("$.data.message").value(CommonErrorCode.METHOD_ARGUMENT_NOT_VALID.message))
        }
    }

    @Nested
    @DisplayName("멤버 상세 조회 API")
    inner class LoadMember {
        private fun getResultActions(token: String): ResultActions =
            mockMvc.perform(
                get("$BASE_MEMBER_URL/me")
                    .header(HttpHeaders.AUTHORIZATION, TokenFixture().authorization(token)),
            )

        @Test
        @DisplayName("인증되지 않은 사용자라면 401 Unauthorized를 반환한다.")
        fun shouldReturnUnauthorizedWhenUnauthenticated() {
            // when
            val resultActions = getResultActions("")

            // then
            resultActions
                .andExpect(status().isUnauthorized)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(AuthErrorCode.UNAUTHENTICATED.status.value()))
                .andExpect(jsonPath("$.data.message").value(AuthErrorCode.UNAUTHENTICATED.message))
        }

        @Test
        @DisplayName("멤버가 이미 삭제되었다면 400 Bad Request를 반환한다.")
        fun shouldReturnBadRequestWhenMemberAlreadyDeleted() {
            // when
            val resultActions = getResultActions(deletedToken)

            // then
            resultActions
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(MemberErrorCode.MEMBER_ALREADY_DELETED.status.value()))
                .andExpect(jsonPath("$.data.message").value(MemberErrorCode.MEMBER_ALREADY_DELETED.message))
        }

        @Test
        @DisplayName("특정 멤버를 상세 조회하고 반환한다.")
        fun shouldReturnMember() {
            // when
            val resultActions = getResultActions(token)

            // then
            resultActions
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.data").isNotEmpty)
        }
    }
}
