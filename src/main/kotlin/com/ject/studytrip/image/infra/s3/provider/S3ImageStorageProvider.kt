package com.ject.studytrip.image.infra.s3.provider

import com.ject.studytrip.image.application.dto.CleanupImagesResult
import com.ject.studytrip.image.application.service.ImageService
import com.ject.studytrip.image.infra.s3.client.S3ImageStorageClient
import com.ject.studytrip.image.infra.s3.dto.ImageHeadInfo
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import software.amazon.awssdk.core.exception.SdkClientException
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse
import software.amazon.awssdk.services.s3.model.ObjectIdentifier
import software.amazon.awssdk.services.s3.model.S3Exception

@Component
class S3ImageStorageProvider(
    private val s3Client: S3ImageStorageClient,
) {
    companion object {
        private val log = LoggerFactory.getLogger(ImageService::class.java)
    }

    fun issuePresignedUrl(key: String): String {
        val presignPut = s3Client.presignPut(key)

        return presignPut.url().toString()
    }

    fun getHeadByKey(key: String?): ImageHeadInfo {
        val head = s3Client.getHeadObject(key)

        return ImageHeadInfo(head.contentLength())
    }

    fun readPrefix(
        key: String?,
        maxBytes: Int,
    ): ByteArray {
        val range = "bytes=0-${maxBytes - 1}"

        return s3Client.getObjectAsBytes(key, range).asByteArray()
    }

    fun deleteByKey(key: String?) {
        s3Client.deleteObject(key)
    }

    fun deleteByKeys(keys: List<String>): CleanupImagesResult {
        val objects = keys.map { ObjectIdentifier.builder().key(it).build() }

        return try {
            val response = s3Client.deleteObjects(objects)
            val failedKeys = extractFailedKeys(response)
            val success = objects.size - failedKeys.size
            CleanupImagesResult(success, failedKeys)
        } catch (e: Exception) {
            // S3 삭제는 멱등이기 때문에 키가 존재하지 않거나, 중복이여도 에러가 발생하지 않음
            // 삭제 시 발생하는 에러는 보통 S3 내부 서버 문제(IO/네트워크) 혹은 인증/자격, 권한, 정책, 상태 등으로 발생
            // 따라서 요청 레벨 실패로 간주하고 배치를 전체 실패로 처리
            when (e) {
                is S3Exception,
                is SdkClientException,
                -> {
                    log.warn("S3 deleteObjects request failure: {}", e.message, e)
                    CleanupImagesResult(0, keys)
                }

                else -> {
                    throw e
                }
            }
        }
    }

    fun copyByKey(
        tmpKey: String?,
        finalKey: String?,
    ) {
        s3Client.copyObject(tmpKey, finalKey)
        s3Client.deleteObject(tmpKey)
    }

    private fun extractFailedKeys(response: DeleteObjectsResponse): List<String> =
        response
            .errors()
            ?.mapNotNull { it.key() }
            ?.filter(String::isNotBlank)
            ?.distinct()
            ?: emptyList()
}
