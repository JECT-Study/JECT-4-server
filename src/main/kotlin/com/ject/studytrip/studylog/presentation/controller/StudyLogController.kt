package com.ject.studytrip.studylog.presentation.controller

import com.ject.studytrip.global.common.response.StandardResponse
import com.ject.studytrip.studylog.application.facade.StudyLogFacade
import com.ject.studytrip.studylog.presentation.dto.request.ConfirmStudyLogImageRequest
import com.ject.studytrip.studylog.presentation.dto.request.CreateStudyLogRequest
import com.ject.studytrip.studylog.presentation.dto.request.PresignStudyLogImageRequest
import com.ject.studytrip.studylog.presentation.dto.response.CreateStudyLogResponse
import com.ject.studytrip.studylog.presentation.dto.response.LoadStudyLogsSliceResponse
import com.ject.studytrip.studylog.presentation.dto.response.PresignedStudyLogImageResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "StudyLog", description = "학습 로그 API")
@RestController
@Validated
class StudyLogController(
    private val studyLogFacade: StudyLogFacade,
) {
    @Operation(summary = "학습 로그 생성", description = "학습을 완료한 데일리 미션을 선택해 학습 로그를 생성합니다.")
    @PostMapping("/api/trips/{tripId}/daily-goals/{dailyGoalId}/study-logs")
    fun createStudyLog(
        @AuthenticationPrincipal memberId: String,
        @PathVariable @NotNull(message = "여행 ID는 필수 요청 파라미터입니다.") tripId: Long,
        @PathVariable @NotNull(message = "데일리 목표 ID는 필수 요청 파라미터입니다.") dailyGoalId: Long,
        @RequestBody @Valid request: CreateStudyLogRequest,
    ): ResponseEntity<StandardResponse> {
        val result = studyLogFacade.createStudyLog(memberId.toLong(), tripId, dailyGoalId, request)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(StandardResponse.success(HttpStatus.CREATED.value(), CreateStudyLogResponse.of(result)))
    }

    @Operation(summary = "학습 로그 목록 조회", description = "특정 여행의 학습 로그 목록을 조회합니다. 슬라이스를 적용하고 정렬 옵션 LATEST(최신순)/OLDEST(과거순)을 적용합니다.")
    @GetMapping("/api/trips/{tripId}/study-logs")
    fun loadStudyLogsByTrip(
        @AuthenticationPrincipal memberId: String,
        @PathVariable @NotNull(message = "여행 ID는 필수 요청 파라미터입니다.") tripId: Long,
        @RequestParam(name = "page", defaultValue = "0") @Min(0) page: Int,
        @RequestParam(name = "size", defaultValue = "5") @Min(1) @Max(10) size: Int,
        @RequestParam(name = "order", defaultValue = "LATEST")
        @Pattern(
            regexp = "LATEST|OLDEST",
            message = "정렬은 최신순(LATEST) 또는 과거순(OLDEST)만 허용됩니다.",
        )
        order: String,
    ): ResponseEntity<StandardResponse> {
        val result = studyLogFacade.getStudyLogsByTrip(memberId.toLong(), tripId, page, size, order)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(StandardResponse.success(HttpStatus.OK.value(), LoadStudyLogsSliceResponse.of(result.studyLogDetails, result.hasNext)))
    }

    @Operation(
        summary = "학습 로그 이미지 업로드용 Presigned URL 발급",
        description = """
                    학습 로그 이미지를 S3에 업로드하기 위한 Presigned URL을 발급합니다.

                    [흐름]
                    1) 먼저 학습 로그 생성 API를 호출해 StudyLogId를 응답받습니다.
                    2) 사용자가 이미지를 첨부했을 경우, 생성 시 받은 StudyLogId를 PathVariable로 전달하여
                    업로드용 파일명 정보를 함께 Presigned URL 발급 API를 요청합니다.
                    서버는 업로드에 사용할 Presigned PUT URL과 임시키(tmpKey)를 반환합니다.
                    3) 반환받은 Presigned URL로 PUT 요청을 통해 이미지를 S3에 업로드합니다.
                    4) 업로드가 정상적으로 완료되면 바로 학습 로그 이미지 Confirm API를 호출합니다.
                    이때 Presigned URL 발급 API에서 반환받은 임시키(tmpKey)를 함께 요청합니다.
                    서버는 업로드된 이미지를 검증(크키/MIME)하고 확정합니다.

                    [주의]
                    - 학습로그 이미지 Presigned URL 발급 요청 API는 StudyLogId가 필요하기 때문에 필수로 본 API를 호출하기 전 학습 로그를 먼저 생성해야합니다.
                    - 요청 값의 originFilename은 꼭 파일 확장자를 포함한 파일명으로 요청해야합니다.
                    - Presigned URL 유효시간은 짧습니다(예: 10분). 만료되면 재발급해야 합니다.
               """,
    )
    @PostMapping("/api/study-logs/{studyLogId}/images/presigned")
    fun presigned(
        @PathVariable @NotNull(message = "학습 로그 ID는 필수 요청 파라미터입니다.") studyLogId: Long,
        @RequestBody @Valid request: PresignStudyLogImageRequest,
    ): ResponseEntity<StandardResponse> {
        val result = studyLogFacade.issuePresignedUrl(studyLogId, request)

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(
                StandardResponse.success(
                    HttpStatus.OK.value(),
                    PresignedStudyLogImageResponse(result.studyLogId, result.tmpKey, result.presignedUrl),
                ),
            )
    }

    @Operation(
        summary = "업로드된 학습 로그 이미지 검증/확정",
        description = """
                    Presigned URL을 통해 S3에 업로드된 학습 로그 이미지를 서버에서 검증하고 확정(Confirm)합니다.

                    [흐름]
                    1) 클라이언트는 발급받은 URL로 이미지를 업로드합니다.
                    2) 업로드 완료 후, Presigned URL 발급 API에서 응답받은 임시키(tmpKey)를 포함해 해당 API를 호출합니다.
                    임시키(tmpKey)는 Presigned URL의 전체 경로 중, 버킷 호스트명을 제외한 S3 객체 경로(ObjectKey) 입니다.
                    (예: https://bucket.s3.ap-northeast-2.amazonaws.com/tmp/study-logs/1/abc.jpg -> tmp/study-logs/1/abc.jpg)

                    3) 서버는 이미지 존재 여부, 크기, MIME 타입 등을 검증한 뒤 최종 경로로 이동시키고 학습 로그 이미지 정보를 갱신합니다.
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
            """,
    )
    @PostMapping("/api/study-logs/{studyLogId}/images/confirm")
    fun confirm(
        @PathVariable @NotNull(message = "학습 로그 ID는 필수 요청 파라미터입니다.") studyLogId: Long,
        @RequestBody @Valid request: ConfirmStudyLogImageRequest,
    ): ResponseEntity<StandardResponse> {
        studyLogFacade.confirmImage(studyLogId, request)

        return ResponseEntity.status(HttpStatus.OK).body(StandardResponse.success(HttpStatus.OK.value(), null))
    }
}
