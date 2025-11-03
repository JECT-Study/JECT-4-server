package com.ject.studytrip.member.presentation.controller;

import com.ject.studytrip.global.common.response.StandardResponse;
import com.ject.studytrip.member.application.dto.MemberDetail;
import com.ject.studytrip.member.application.dto.PresignedProfileImageInfo;
import com.ject.studytrip.member.application.facade.MemberFacade;
import com.ject.studytrip.member.presentation.dto.request.ConfirmProfileImageRequest;
import com.ject.studytrip.member.presentation.dto.request.PresignProfileImageRequest;
import com.ject.studytrip.member.presentation.dto.request.UpdateMemberRequest;
import com.ject.studytrip.member.presentation.dto.response.LoadMemberDetailResponse;
import com.ject.studytrip.member.presentation.dto.response.PresignProfileImageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Member", description = "멤버 API")
@RequestMapping("/api/members")
@RestController
@RequiredArgsConstructor
@Validated
public class MemberController {
    private final MemberFacade memberFacade;

    @Operation(summary = "멤버 수정", description = "멤버의 이름 또는 카테고리를 수정합니다.")
    @PatchMapping("/me")
    public ResponseEntity<StandardResponse> updateMember(
            @AuthenticationPrincipal String memberId, @RequestBody UpdateMemberRequest request) {
        memberFacade.updateNicknameAndCategoryIfPresent(Long.valueOf(memberId), request);

        return ResponseEntity.status(HttpStatus.OK)
                .body(StandardResponse.success(HttpStatus.OK.value(), null));
    }

    @Operation(summary = "멤버 삭제", description = "멤버를 삭제합니다. (회원탈퇴)")
    @DeleteMapping("/me")
    public ResponseEntity<StandardResponse> deleteMember(@AuthenticationPrincipal String memberId) {
        memberFacade.deleteMember(Long.valueOf(memberId));

        return ResponseEntity.status(HttpStatus.OK)
                .body(StandardResponse.success(HttpStatus.OK.value(), null));
    }

    @Operation(summary = "멤버 상세 조회", description = "멤버를 상세 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<StandardResponse> loadMemberDetail(
            @AuthenticationPrincipal String memberId) {
        MemberDetail result = memberFacade.getMemberDetail(Long.valueOf(memberId));

        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        StandardResponse.success(
                                HttpStatus.OK.value(),
                                LoadMemberDetailResponse.of(
                                        result.memberInfo(),
                                        result.tripCount(),
                                        result.studyLogCount())));
    }

    @Operation(
            summary = "멤버 프로필 이미지 업로드용 Presigned URL 발급",
            description =
                    """
                                 멤버 프로필 이미지를 S3 Storage에 업로드하기 위한 Presigned URL을 발급합니다.

                                 [흐름]
                                 1) 멤버 수정 화면에서 수정하기 버튼을 클릭합니다.
                                 2) 이때 만약 프로필 이미지를 변경했다면 해당 파일이름과 함께 본 API를 호출합니다.(예: abc.jpeg 형태)
                                 2-1) 프로필 이미지를 변경하지 않았으면 멤버 수정 API를 호출합니다. (닉네임 또는 카테고리를 수정했을 경우)
                                 3) 서버는 업로드에 사용할 Presigned PUT URL과 임시 키(tmpKey)를 반환합니다.
                                 4) 클라이언트는 반환된 Presigned URL로 이미지를 S3에 업로드합니다.
                                 5) 업로드가 정상 완료되면 즉시 프로필 이미지 Confirm API를 호출하여 이미지를 검증 및 확정하고 멤버 프로필에 적용합니다.
                                 6) 이후 다른 수정 사항이 있다면 멤버 수정 API를 호출합니다. (닉네임 또는 카테고리도 수정했을 경우)

                                 [주의]
                                 - Presigned URL 유효시간은 짧습니다(예: 10분). 만료되면 재발급해야 합니다.
                                 - 요청 값의 originFilename은 꼭 파일 확장자를 포함한 파일명으로 요청해야합니다.
                            """)
    @PostMapping("/profile-images/presigned")
    public ResponseEntity<StandardResponse> presigned(
            @AuthenticationPrincipal String memberId,
            @RequestBody @Valid PresignProfileImageRequest request) {
        PresignedProfileImageInfo info =
                memberFacade.issuePresignedUrl(Long.valueOf(memberId), request);
        return ResponseEntity.ok()
                .body(
                        StandardResponse.success(
                                HttpStatus.OK.value(),
                                PresignProfileImageResponse.of(
                                        info.memberId(), info.tmpKey(), info.presignedUrl())));
    }

    @Operation(
            summary = "업로드된 멤버 프로필 이미지 검증/확정",
            description =
                    """
                    Presigned URL을 통해 S3에 업로드된 프로필 이미지를 서버에서 검증하고 확정(Confirm)합니다.

                    [흐름]
                    1) 클라이언트는 발급받은 Presigned PUT URL로 이미지를 업로드합니다.
                    2) 업로드 완료 후, Presigned URL 발급 API에서 응답받은 임시키(tmpKey)를 포함해 해당 API를 호출합니다.
                    임시키(tmpKey)는 Presigned URL의 전체 경로 중, 버킷 호스트명을 제외한 S3 객체 경로(ObjectKey) 입니다.
                    (예: https://bucket.s3.ap-northeast-2.amazonaws.com/tmp/members/1/abc.jpg -> tmp/members/1/abc.jpg)

                    3) 서버는 이미지 존재 여부, 크기, MIME 타입 등을 검증한 뒤 최종 경로로 이동시키고 회원 프로필 이미지 정보를 갱신합니다.
                    만약 S3 Storage 기술 자체 에러가 발생하면 임시 경로에 저장된 이미지를 즉시 삭제하지 않아 컨펌 재시도가 가능하지만,
                    유효하지 않은 이미지 크기/확장자 등 도메인 정책을 위반해 실패할 경우 임시 경로에 저장된 이미지가 즉시 삭제되며 다시 업로드부터 수행해야합니다.

                    S3 Storage 기술 자체 예외 예시
                    {
                        "status": 502 (BAD_GATEWAY),
                        "message": "Storage 서버 에러가 발생했습니다."
                    }

                    이미지 도메인 정책 위반 예외 예시
                    {
                        "status": 400 (BAD_REQUEST),
                        "message": "유효하지 않은 이미지 확장자 입니다." , "유효하지 않은 이미지 MIME 입니다." 등
                    }

                    [주의]
                    - 이미지 타입(MIME/Content-Type)은 JPG, JPEG, PNG, WEBP만 허용합니다. 그 외 타입은 도메인 정책 위반으로 예외가 발생합니다.
                    - 이미지 최대 크기는 5MB로 설정되어있으며, 크기가 0 이하이거나 최대 크기를 벗어날 경우 도메인 정책 위반으로 예외가 발생합니다.
                    - 업로드는 되었지만 그 이후 문제가 발생하더라고 tmp/ 경로의 객체는 라이프사이클 정책에 따라 자동 정리되기 때문에 따로 삭제 요청 API는 호출하지 않아도 됩니다.
            """)
    @PostMapping("/profile-images/confirm")
    public ResponseEntity<StandardResponse> confirm(
            @AuthenticationPrincipal String memberId,
            @RequestBody @Valid ConfirmProfileImageRequest request) {
        memberFacade.confirmImage(Long.valueOf(memberId), request);
        return ResponseEntity.ok().body(StandardResponse.success(HttpStatus.OK.value(), null));
    }

    @Operation(summary = "멤버 즉시 삭제", description = "멤버를 즉시 삭제하고 관련된 모든 데이터를 삭제합니다. (CASCADE)")
    @DeleteMapping("/me/hard-delete")
    public ResponseEntity<StandardResponse> deleteMemberHardDelete(
            @AuthenticationPrincipal String memberId) {
        memberFacade.hardDeleteMemberCascade(Long.valueOf(memberId));

        return ResponseEntity.ok().body(StandardResponse.success(HttpStatus.OK.value(), null));
    }
}
