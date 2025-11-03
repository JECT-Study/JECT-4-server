package com.ject.studytrip.member.presentation.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ject.studytrip.BaseIntegrationTest;
import com.ject.studytrip.auth.domain.error.AuthErrorCode;
import com.ject.studytrip.auth.fixture.TokenFixture;
import com.ject.studytrip.auth.helper.TokenTestHelper;
import com.ject.studytrip.image.domain.error.ImageErrorCode;
import com.ject.studytrip.image.infra.s3.provider.S3ImageStorageProvider;
import com.ject.studytrip.member.domain.error.MemberErrorCode;
import com.ject.studytrip.member.domain.model.Member;
import com.ject.studytrip.member.fixture.UpdateMemberRequestFixture;
import com.ject.studytrip.member.helper.MemberTestHelper;
import com.ject.studytrip.member.presentation.dto.request.ConfirmProfileImageRequest;
import com.ject.studytrip.member.presentation.dto.request.PresignProfileImageRequest;
import com.ject.studytrip.member.presentation.dto.request.UpdateMemberRequest;
import com.ject.studytrip.trip.domain.model.TripCategory;
import com.ject.studytrip.trip.helper.TripTestHelper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;

@DisplayName("MemberController 통합 테스트")
class MemberControllerIntegrationTest extends BaseIntegrationTest {
    private static final String BASE_MEMBER_URL = "/api/members";

    @Autowired private MemberTestHelper memberTestHelper;
    @Autowired private TokenTestHelper tokenTestHelper;
    @Autowired private TripTestHelper tripTestHelper;

    @MockitoBean S3ImageStorageProvider s3ImageStorageProvider;

    private Member member;
    private String accessToken;

    @BeforeEach
    void setUp() {
        member = memberTestHelper.saveMember();
        accessToken =
                tokenTestHelper.createAccessToken(
                        member.getId().toString(), member.getRole().name());
        tripTestHelper.saveTrip(member, TripCategory.COURSE);
        tripTestHelper.saveTrip(member, TripCategory.COURSE);
        tripTestHelper.saveTrip(member, TripCategory.COURSE);
        tripTestHelper.saveTrip(member, TripCategory.EXPLORE);
        tripTestHelper.saveTrip(member, TripCategory.EXPLORE);
    }

    @Nested
    @DisplayName("멤버 수정 API")
    class UpdateMember {
        private final UpdateMemberRequestFixture fixture = new UpdateMemberRequestFixture();

        private ResultActions getResultActions(String accessToken, UpdateMemberRequest request)
                throws Exception {
            return mockMvc.perform(
                    patch(BASE_MEMBER_URL + "/me")
                            .header(
                                    HttpHeaders.AUTHORIZATION,
                                    TokenFixture.TOKEN_PREFIX + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)));
        }

        @Test
        @DisplayName("Access Token이 없으면 401 Unauthorized를 반환한다.")
        void shouldReturnUnauthorizedWhenAccessTokenIsMissing() throws Exception {
            // given
            UpdateMemberRequest request = fixture.withNickname("새로운 닉네임").build();

            // when
            ResultActions resultActions = getResultActions("", request);

            // then
            resultActions
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(AuthErrorCode.UNAUTHENTICATED.getStatus().value()))
                    .andExpect(
                            jsonPath("$.data.message")
                                    .value(AuthErrorCode.UNAUTHENTICATED.getMessage()));
        }

        @Test
        @DisplayName("삭제된 여행일 경우 404 Not Found를 반환한다.")
        void shouldReturnNotFoundWhenMemberAlreadyDeleted() throws Exception {
            // given
            UpdateMemberRequest request = fixture.withNickname("새로운 닉네임").build();
            member.updateDeletedAt();

            // when
            ResultActions resultActions = getResultActions(accessToken, request);

            // then
            resultActions
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(MemberErrorCode.MEMBER_NOT_FOUND.getStatus().value()))
                    .andExpect(
                            jsonPath("$.data.message")
                                    .value(MemberErrorCode.MEMBER_NOT_FOUND.getMessage()));
        }

        @Test
        @DisplayName("유효한 요청이 들어오면 멤버 닉네임을 수정한다.")
        void shouldUpdateMemberNicknameWhenRequestIsValid() throws Exception {
            // given
            UpdateMemberRequest request = fixture.withNickname("새로운 닉네임").build();

            // when
            ResultActions resultActions = getResultActions(accessToken, request);

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()));
        }

        @Test
        @DisplayName("유효한 요청이 들어오면 멤버 카테고리를 수정한다.")
        void shouldUpdateMemberCategoryWhenRequestIsValid() throws Exception {
            // given
            UpdateMemberRequest request = fixture.withCategory("WORKER").build();

            // when
            ResultActions resultActions = getResultActions(accessToken, request);

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()));
        }

        @Test
        @DisplayName("유효한 요청이 들어오면 멤버 닉네임과 카테고리를 수정한다.")
        void shouldUpdateMemberNicknameAndCategoryWhenRequestIsValid() throws Exception {
            // given
            UpdateMemberRequest request =
                    fixture.withNickname("새로운 닉네임").withCategory("WORKER").build();

            // when
            ResultActions resultActions = getResultActions(accessToken, request);

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()));
        }
    }

    @Nested
    @DisplayName("멤버 삭제 API")
    class DeleteMember {
        private ResultActions getResultActions(String accessToken) throws Exception {
            return mockMvc.perform(
                    delete(BASE_MEMBER_URL + "/me")
                            .header(
                                    HttpHeaders.AUTHORIZATION,
                                    TokenFixture.TOKEN_PREFIX + accessToken)
                            .contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        @DisplayName("Access Token이 없으면 401 Unauthorized를 반환한다.")
        void shouldReturnUnauthorizedWhenAccessTokenIsMissing() throws Exception {
            // when
            ResultActions resultActions = getResultActions("");

            // then
            resultActions
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(AuthErrorCode.UNAUTHENTICATED.getStatus().value()))
                    .andExpect(
                            jsonPath("$.data.message")
                                    .value(AuthErrorCode.UNAUTHENTICATED.getMessage()));
        }

        @Test
        @DisplayName("삭제된 여행일 경우 404 Not Found를 반환한다.")
        void shouldReturnBadRequestWhenMemberAlreadyDeleted() throws Exception {
            // given
            member.updateDeletedAt();

            // when
            ResultActions resultActions = getResultActions(accessToken);

            // then
            resultActions
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(MemberErrorCode.MEMBER_NOT_FOUND.getStatus().value()))
                    .andExpect(
                            jsonPath("$.data.message")
                                    .value(MemberErrorCode.MEMBER_NOT_FOUND.getMessage()));
        }

        @Test
        @DisplayName("유효한 멤버 ID가 들어오면 멤버를 삭제한다.")
        void shouldDeleteMemberWhenMemberIdIsValid() throws Exception {
            // when
            ResultActions resultActions = getResultActions(accessToken);

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()));
        }
    }

    @Nested
    @DisplayName("멤버 상세 조회 API")
    class LoadMemberDetail {
        private ResultActions getResultActions(String accessToken) throws Exception {
            return mockMvc.perform(
                    get(BASE_MEMBER_URL + "/me")
                            .header(
                                    HttpHeaders.AUTHORIZATION,
                                    TokenFixture.TOKEN_PREFIX + accessToken)
                            .contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        @DisplayName("Access Token이 없으면 401 Unauthorized를 반환한다.")
        void shouldReturnUnauthorizedWhenAccessTokenIsMissing() throws Exception {
            // when
            ResultActions resultActions = getResultActions("");

            // then
            resultActions
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(AuthErrorCode.UNAUTHENTICATED.getStatus().value()))
                    .andExpect(
                            jsonPath("$.data.message")
                                    .value(AuthErrorCode.UNAUTHENTICATED.getMessage()));
        }

        @Test
        @DisplayName("삭제된 여행일 경우 404 Not Found를 반환한다.")
        void shouldReturnBadRequestWhenMemberAlreadyDeleted() throws Exception {
            // given
            member.updateDeletedAt();

            // when
            ResultActions resultActions = getResultActions(accessToken);

            // then
            resultActions
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(MemberErrorCode.MEMBER_NOT_FOUND.getStatus().value()))
                    .andExpect(
                            jsonPath("$.data.message")
                                    .value(MemberErrorCode.MEMBER_NOT_FOUND.getMessage()));
        }

        @Test
        @DisplayName("유효한 멤버 ID가 들어오면 멤버 상세 정보를 반환한다.")
        void shouldReturnMemberDetailWhenMemberIdIsValid() throws Exception {
            // when
            ResultActions resultActions = getResultActions(accessToken);

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                    .andExpect(jsonPath("$.data.memberId").value(member.getId()))
                    .andExpect(jsonPath("$.data.email").value(member.getEmail()))
                    .andExpect(jsonPath("$.data.nickname").value(member.getNickname()))
                    .andExpect(jsonPath("$.data.profileImage").value(member.getProfileImage()))
                    .andExpect(jsonPath("$.data.category").value(member.getCategory().name()))
                    .andExpect(jsonPath("$.data.courseTripCount").value(3))
                    .andExpect(jsonPath("$.data.exploreTripCount").value(2))
                    .andExpect(jsonPath("$.data.studyLogCount").value(0));
        }
    }

    @Nested
    @DisplayName("프로필 이미지 Presigned URL 발급 API")
    class IssuePresignedUrl {
        private ResultActions getResultActions(
                String accessToken, PresignProfileImageRequest request) throws Exception {
            return mockMvc.perform(
                    post(BASE_MEMBER_URL + "/profile-images/presigned")
                            .header(
                                    HttpHeaders.AUTHORIZATION,
                                    TokenFixture.TOKEN_PREFIX + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)));
        }

        @Test
        @DisplayName("Access Token이 없으면 401 Unauthorized를 반환한다")
        void shouldReturnUnauthorizedWhenAccessTokenIsMissing() throws Exception {
            // given
            PresignProfileImageRequest request = new PresignProfileImageRequest("test.jpg");

            // when
            ResultActions resultActions = getResultActions("", request);

            // then
            resultActions
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(AuthErrorCode.UNAUTHENTICATED.getStatus().value()))
                    .andExpect(
                            jsonPath("$.data.message")
                                    .value(AuthErrorCode.UNAUTHENTICATED.getMessage()));
        }

        @Test
        @DisplayName("유효한 파일명으로 Presigned URL을 발급한다")
        void shouldIssuePresignedUrlWhenFilenameIsValid() throws Exception {
            // given
            PresignProfileImageRequest request = new PresignProfileImageRequest("profile.jpg");
            given(s3ImageStorageProvider.issuePresignedUrl(anyString()))
                    .willReturn("https://mocked-presigned-url.com");

            // when
            ResultActions resultActions = getResultActions(accessToken, request);

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                    .andExpect(jsonPath("$.data.presignedUrl").isNotEmpty())
                    .andExpect(jsonPath("$.data.tmpKey").isNotEmpty())
                    .andExpect(jsonPath("$.data.tmpKey").value(Matchers.startsWith("tmp/members/")))
                    .andExpect(
                            jsonPath("$.data.tmpKey")
                                    .value(Matchers.containsString(member.getId().toString())));

            // S3Provider 호출 검증
            verify(s3ImageStorageProvider).issuePresignedUrl(anyString());
        }

        @Test
        @DisplayName("파일명이 비어있으면 400 Bad Request를 반환한다")
        void shouldReturnBadRequestWhenFilenameIsEmpty() throws Exception {
            // given
            PresignProfileImageRequest request = new PresignProfileImageRequest("");

            // when
            ResultActions resultActions = getResultActions(accessToken, request);

            // then
            resultActions.andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("유효하지 않은 확장자는 400 Bad Request를 반환한다")
        void shouldReturnBadRequestWhenExtensionIsInvalid() throws Exception {
            // given
            PresignProfileImageRequest request = new PresignProfileImageRequest("profile.txt");

            // when
            ResultActions resultActions = getResultActions(accessToken, request);

            // then
            resultActions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(
                                            ImageErrorCode.INVALID_IMAGE_EXTENSION
                                                    .getStatus()
                                                    .value()))
                    .andExpect(
                            jsonPath("$.data.message")
                                    .value(ImageErrorCode.INVALID_IMAGE_EXTENSION.getMessage()));
        }
    }

    @Nested
    @DisplayName("프로필 이미지 확정 API")
    class ConfirmProfileImage {
        private ResultActions getResultActions(
                String accessToken, ConfirmProfileImageRequest request) throws Exception {
            return mockMvc.perform(
                    post(BASE_MEMBER_URL + "/profile-images/confirm")
                            .header(
                                    HttpHeaders.AUTHORIZATION,
                                    TokenFixture.TOKEN_PREFIX + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)));
        }

        @Test
        @DisplayName("Access Token이 없으면 401 Unauthorized를 반환한다")
        void shouldReturnUnauthorizedWhenAccessTokenIsMissing() throws Exception {
            // given
            ConfirmProfileImageRequest request =
                    new ConfirmProfileImageRequest("tmp/members/1/test.jpg");

            // when
            ResultActions resultActions = getResultActions("", request);

            // then
            resultActions
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(AuthErrorCode.UNAUTHENTICATED.getStatus().value()))
                    .andExpect(
                            jsonPath("$.data.message")
                                    .value(AuthErrorCode.UNAUTHENTICATED.getMessage()));
        }

        @Test
        @DisplayName("tmpKey가 비어있으면 400 Bad Request를 반환한다")
        void shouldReturnBadRequestWhenTmpKeyIsEmpty() throws Exception {
            // given
            ConfirmProfileImageRequest request = new ConfirmProfileImageRequest("");

            // when
            ResultActions resultActions = getResultActions(accessToken, request);

            // then
            resultActions.andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("멤버 즉시 삭제 API")
    class DeleteMemberHardDelete {
        private ResultActions getResultActions(String accessToken) throws Exception {
            return mockMvc.perform(
                    delete(BASE_MEMBER_URL + "/me/hard-delete")
                            .header(
                                    HttpHeaders.AUTHORIZATION,
                                    TokenFixture.TOKEN_PREFIX + accessToken)
                            .contentType(MediaType.APPLICATION_JSON));
        }

        @Test
        @DisplayName("Access Token이 없으면 401 Unauthorized를 반환한다.")
        void shouldReturnUnauthorizedWhenAccessTokenIsMissing() throws Exception {
            // when
            ResultActions resultActions = getResultActions("");

            // then
            resultActions
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(AuthErrorCode.UNAUTHENTICATED.getStatus().value()))
                    .andExpect(
                            jsonPath("$.data.message")
                                    .value(AuthErrorCode.UNAUTHENTICATED.getMessage()));
        }

        @Test
        @DisplayName("삭제된 멤버일 경우 404 Not Found를 반환한다.")
        void shouldReturnNotFoundWhenMemberAlreadyDeleted() throws Exception {
            // given
            member.updateDeletedAt();

            // when
            ResultActions resultActions = getResultActions(accessToken);

            // then
            resultActions
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(
                            jsonPath("$.status")
                                    .value(MemberErrorCode.MEMBER_NOT_FOUND.getStatus().value()))
                    .andExpect(
                            jsonPath("$.data.message")
                                    .value(MemberErrorCode.MEMBER_NOT_FOUND.getMessage()));
        }

        @Test
        @DisplayName("유효한 멤버 ID가 들어오면 멤버와 관련된 모든 데이터를 즉시 삭제한다.")
        void shouldHardDeleteMemberAndAllRelatedDataWhenMemberIdIsValid() throws Exception {
            // when
            ResultActions resultActions = getResultActions(accessToken);

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()));
        }
    }
}
