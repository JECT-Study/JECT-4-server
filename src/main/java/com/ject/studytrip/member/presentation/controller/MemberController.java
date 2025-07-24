package com.ject.studytrip.member.presentation.controller;

import com.ject.studytrip.global.common.response.StandardResponse;
import com.ject.studytrip.member.application.dto.MemberDetail;
import com.ject.studytrip.member.application.facade.MemberFacade;
import com.ject.studytrip.member.presentation.dto.request.UpdateMemberRequest;
import com.ject.studytrip.member.presentation.dto.response.LoadMemberDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
}
