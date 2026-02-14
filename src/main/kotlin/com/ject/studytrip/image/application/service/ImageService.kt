package com.ject.studytrip.image.application.service

import com.ject.studytrip.global.config.properties.CdnProperties
import com.ject.studytrip.global.exception.CustomException
import com.ject.studytrip.global.util.FilenameUtil
import com.ject.studytrip.image.application.dto.CleanupImagesResult
import com.ject.studytrip.image.application.dto.PresignedImageInfo
import com.ject.studytrip.image.application.event.ImageEventPublisher
import com.ject.studytrip.image.domain.constants.ImageConstants
import com.ject.studytrip.image.domain.factory.ImageKeyFactory
import com.ject.studytrip.image.domain.policy.ImagePolicy
import com.ject.studytrip.image.domain.util.ImageUrlUtil
import com.ject.studytrip.image.infra.s3.provider.S3ImageStorageProvider
import com.ject.studytrip.image.infra.tika.provider.TikaImageProbeProvider
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.math.min

@Service
class ImageService(
    private val s3Provider: S3ImageStorageProvider,
    private val tikaProvider: TikaImageProbeProvider,
    private val publishers: ImageEventPublisher,
    private val cdnProperties: CdnProperties,
) {
    companion object {
        private const val MAX_BATCH = 1000
        private val log = LoggerFactory.getLogger(ImageService::class.java)
    }

    // Presigned URL 발급
    fun presign(
        keyPrefix: String?,
        id: String,
        originFilename: String,
    ): PresignedImageInfo {
        // 키 prefix 검증
        ImagePolicy.validateKeyPrefix(keyPrefix)

        // 확장자 추출, 검증
        val ext = FilenameUtil.extractExtension(originFilename)
        ImagePolicy.validateExtension(ext)

        // 새로운 파일명 생성
        val filename = FilenameUtil.createNewFilename(ext)

        // 임시 키 생성
        val tmpKey = ImageKeyFactory.createTmpKey(keyPrefix, id, filename)

        // Presigned URL 생성
        val presignedUrl = s3Provider.issuePresignedUrl(tmpKey)

        return PresignedImageInfo(tmpKey, presignedUrl)
    }

    // 업로드된 이미지 확정
    // S3 자체 에러 시에는 cleanup 실행 X
    // 이미지 파일 크기, MIME 등 도메인 정책 검증에 실패하면 cleanup 실행
    fun confirm(tmpKey: String): String {
        // 임시 이미지 키 검증
        ImagePolicy.validateKey(tmpKey)

        // 업로드된 이미지 HEAD 조회
        val head = s3Provider.getHeadByKey(tmpKey)

        // 이미지 크기 검증, 검증 실패 시 이미지 삭제
        validateSizeWithCleanup(tmpKey, head.contentLength)

        // MIME 추출 및 판별, 검증 실패 시 이미지 삭제
        validateMimeWithCleanup(tmpKey, head.contentLength)

        // 임시 -> 최종 이미지 복사 및 경로 반환
        return moveToFinalLocation(tmpKey)
    }

    // 업로드 취소
    fun cancel(uploadKeys: List<String>) {
        s3Provider.deleteByKeys(uploadKeys)
    }

    // 이미지 삭제
    fun cleanup(imageUrl: String?) {
        ImageUrlUtil.extractKey(cdnProperties.domain, imageUrl)?.let { s3Provider.deleteByKey(it) }
    }

    // 이미지 배치 삭제
    fun cleanupBatch(imageUrls: List<String>) {
        val keys = extractKeysFromUrls(imageUrls)
        if (keys.isEmpty()) return

        var attempted = 0
        var succeeded = 0
        val failed = mutableListOf<String>()

        keys.chunked(MAX_BATCH).forEach { batch ->
            attempted += batch.size
            val result: CleanupImagesResult = s3Provider.deleteByKeys(batch)
            succeeded += result.success

            if (result.failedKeys.isNotEmpty()) {
                failed += result.failedKeys
            }
        }

        log.info("Image Cleanup Batch attempted={}, succeeded={}, failed={}", attempted, succeeded, failed.size)

        if (failed.isNotEmpty()) {
            log.debug("Image Cleanup Batch Failed. failedCount={}, failedKeys={}", failed.size, failed)
        }
    }

    fun publishCleanupBatchEvent(imageUrls: List<String>?) {
        publishers.publishCleanupBatch(imageUrls)
    }

    // 이미지 사이즈 검증, 실패 시 삭제
    private fun validateSizeWithCleanup(
        tmpKey: String?,
        contentLength: Long,
    ) {
        try {
            ImagePolicy.validateSize(contentLength)
        } catch (e: CustomException) {
            cleanupAndThrow(tmpKey, e)
        }
    }

    // 이미지 MIME 추출 및 검증, 실패 시 삭제
    private fun validateMimeWithCleanup(
        tmpKey: String?,
        len: Long,
    ) {
        val maxBytes = min(len, ImageConstants.PROBE_BYTES.toLong()).toInt()
        val prefix = s3Provider.readPrefix(tmpKey, maxBytes)
        val mime = tikaProvider.detectMime(prefix)

        try {
            ImagePolicy.validateMime(mime)
        } catch (e: CustomException) {
            cleanupAndThrow(tmpKey, e)
        }
    }

    // 삭제 및 예외 처리
    private fun cleanupAndThrow(
        tmpKey: String?,
        exception: CustomException,
    ) {
        s3Provider.deleteByKey(tmpKey)
        throw exception
    }

    // 최종 경로에 이미지 복사 및 반환
    private fun moveToFinalLocation(tmpKey: String): String {
        val finalKey = ImageKeyFactory.toFinalKey(tmpKey)
        ImagePolicy.validateKey(finalKey)

        s3Provider.copyByKey(tmpKey, finalKey)

        return ImageUrlUtil.build(cdnProperties.domain, finalKey)
    }

    // 중복, 빈 값 제거 후 키 목록 추출
    private fun extractKeysFromUrls(urls: List<String>?): List<String> {
        if (urls.isNullOrEmpty()) return emptyList()

        return urls
            .asSequence()
            .mapNotNull { ImageUrlUtil.extractKey(cdnProperties.domain, it) }
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .toList()
    }
}
