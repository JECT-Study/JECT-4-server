package com.ject.studytrip.image.application.service

import com.ject.studytrip.BaseUnitTest
import com.ject.studytrip.global.config.properties.CdnProperties
import com.ject.studytrip.global.exception.CustomException
import com.ject.studytrip.image.application.dto.CleanupImagesResult
import com.ject.studytrip.image.application.event.ImageEventPublisher
import com.ject.studytrip.image.domain.error.ImageErrorCode
import com.ject.studytrip.image.fixture.ImageHeadInfoFixture
import com.ject.studytrip.image.fixture.ImageTestConstants.FINAL_KEY
import com.ject.studytrip.image.fixture.ImageTestConstants.INVALID_MIME
import com.ject.studytrip.image.fixture.ImageTestConstants.INVALID_ORIGINAL_FILENAME
import com.ject.studytrip.image.fixture.ImageTestConstants.JPEG_HEADER_BYTES
import com.ject.studytrip.image.fixture.ImageTestConstants.ORIGINAL_FILENAME
import com.ject.studytrip.image.fixture.ImageTestConstants.PRESIGNED_URL
import com.ject.studytrip.image.fixture.ImageTestConstants.TMP_KEY
import com.ject.studytrip.image.fixture.ImageTestConstants.VALID_CONTENT_LENGTH
import com.ject.studytrip.image.fixture.ImageTestConstants.VALID_ID
import com.ject.studytrip.image.fixture.ImageTestConstants.VALID_KEY_PREFIX
import com.ject.studytrip.image.fixture.ImageTestConstants.VALID_MIME
import com.ject.studytrip.image.infra.s3.error.S3ErrorCode
import com.ject.studytrip.image.infra.s3.provider.S3ImageStorageProvider
import com.ject.studytrip.image.infra.tika.provider.TikaImageProbeProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeast
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@DisplayName("ImageService 단위 테스트")
class ImageServiceTest : BaseUnitTest() {
    @InjectMocks
    private lateinit var imageService: ImageService

    @Mock
    private lateinit var s3Provider: S3ImageStorageProvider

    @Mock
    private lateinit var tikaProvider: TikaImageProbeProvider

    @Mock
    private lateinit var cdnProperties: CdnProperties

    @Mock
    private lateinit var imageEventPublisher: ImageEventPublisher

    companion object {
        private const val IMAGE_BASE_URL = "https://test-cdn.cloudfront.net"
        private const val EXTRACTED_KEY = "members/1/test.jpg"
        private const val VALID_IMAGE_URL1 = "$IMAGE_BASE_URL/members/1/image1.jpg"
        private const val VALID_IMAGE_URL2 = "$IMAGE_BASE_URL/members/1/image2.jpg"
        private const val VALID_KEY1 = "members/1/image1.jpg"
        private const val VALID_KEY2 = "members/1/image2.jpg"
    }

    @Nested
    @DisplayName("presign 메서드는")
    inner class Presign {
        @Test
        @DisplayName("유효하지 않은 키 Prefix로 호출하면 예외가 발생한다.")
        fun shouldThrowExceptionWhenKeyPrefixIsInvalid() {
            // when
            val exception = assertThrows<CustomException> { imageService.presign(null, VALID_ID, ORIGINAL_FILENAME) }

            // then
            assertThat(exception.message).isEqualTo(ImageErrorCode.INVALID_IMAGE_KEY_PREFIX.message)
        }

        @Test
        @DisplayName("존재하지 않는 키 Prefix로 호출하면 예외가 발생한다.")
        fun shouldThrowExceptionWhenKeyPrefixDoesNotExist() {
            // when
            val exception = assertThrows<CustomException> { imageService.presign("invalid-key-prefix/", VALID_ID, ORIGINAL_FILENAME) }

            // then
            assertThat(exception.message).isEqualTo(ImageErrorCode.INVALID_IMAGE_KEY_PREFIX.message)
        }

        @Test
        @DisplayName("유효하지 않은 이미지 파일 확장자라면 예외가 발생한다.")
        fun shouldThrowExceptionWhenExtensionIsInvalid() {
            // when
            val exception = assertThrows<CustomException> { imageService.presign(VALID_KEY_PREFIX, VALID_ID, INVALID_ORIGINAL_FILENAME) }

            // then
            assertThat(exception.message).isEqualTo(ImageErrorCode.INVALID_IMAGE_EXTENSION.message)
        }

        @Test
        @DisplayName("유효한 파라미터로 presigned URL을 발급한다.")
        fun shouldIssuePresignedUrlWithValidParameters() {
            // given
            given(s3Provider.issuePresignedUrl(anyString())).willReturn(PRESIGNED_URL)

            // when
            val result = imageService.presign(VALID_KEY_PREFIX, VALID_ID, ORIGINAL_FILENAME)

            // then
            assertThat(result.presignedUrl).isEqualTo(PRESIGNED_URL)
            verify(s3Provider).issuePresignedUrl(anyString())
        }
    }

    @Nested
    @DisplayName("confirm 메서드는")
    inner class Confirm {
        val fixture = ImageHeadInfoFixture()

        @Test
        @DisplayName("tmpKey가 빈 문자열이면 예외가 발생한다.")
        fun shouldThrowExceptionWhenTmpKeyIsBlank() {
            // when
            val exception = assertThrows<CustomException> { imageService.confirm("   ") }

            // then
            assertThat(exception.message).isEqualTo(ImageErrorCode.INVALID_IMAGE_KEY.message)
            verify(s3Provider, never()).deleteByKey(anyString()) // cleanup 비호출 여부
        }

        @Test
        @DisplayName("S3 작업 중 에러가 발생하면 예외가 발생한다.")
        fun shouldThrowExceptionWhenS3OperationFails() {
            // given
            given(s3Provider.getHeadByKey(TMP_KEY)).willThrow(CustomException(S3ErrorCode.S3_STORAGE_SERVER_ERROR))

            // when
            val exception = assertThrows<CustomException> { imageService.confirm(TMP_KEY) }

            // then
            assertThat(exception.message).isEqualTo(S3ErrorCode.S3_STORAGE_SERVER_ERROR.message)
            verify(s3Provider, never()).deleteByKey(anyString()) // cleanup 비호출 여부
        }

        @Test
        @DisplayName("이미지 크기가 유효하지 않으면 cleanup 이후 예외가 발생한다.")
        fun shouldCleanupAndThrowExceptionWhenImageSizeIsInvalid() {
            // given
            val headInfo = fixture.createLarge()
            given(s3Provider.getHeadByKey(TMP_KEY)).willReturn(headInfo)

            // when
            val exception = assertThrows<CustomException> { imageService.confirm(TMP_KEY) }

            // then
            assertThat(exception.message).isEqualTo(ImageErrorCode.IMAGE_SIZE_EXCEEDED.message)
            verify(s3Provider).deleteByKey(TMP_KEY) // cleanup 호출 여부
        }

        @Test
        @DisplayName("이미지 크기가 0이면 cleanup 이후 예외가 발생한다.")
        fun shouldCleanupAndThrowExceptionWhenImageSizeIsZero() {
            // given
            val headInfo = fixture.createEmpty()
            given(s3Provider.getHeadByKey(TMP_KEY)).willReturn(headInfo)

            // when
            val exception = assertThrows<CustomException> { imageService.confirm(TMP_KEY) }

            // then
            assertThat(exception.message).isEqualTo(ImageErrorCode.EMPTY_IMAGE.message)
            verify(s3Provider).deleteByKey(TMP_KEY) // cleanup 호출 여부
        }

        @Test
        @DisplayName("MIME 타입이 유효하지 않으면 cleanup 이후 예외가 발생한다.")
        fun shouldCleanupAndThrowExceptionWhenMimeIsInvalid() {
            // given
            val headInfo = fixture.create()
            given(s3Provider.getHeadByKey(TMP_KEY)).willReturn(headInfo)
            given(s3Provider.readPrefix(TMP_KEY, VALID_CONTENT_LENGTH.toInt())).willReturn(JPEG_HEADER_BYTES)
            given(tikaProvider.detectMime(JPEG_HEADER_BYTES)).willReturn(INVALID_MIME)

            // when
            val exception = assertThrows<CustomException> { imageService.confirm(TMP_KEY) }

            // then
            assertThat(exception.message).isEqualTo(ImageErrorCode.INVALID_IMAGE_MIME.message)
            verify(s3Provider).deleteByKey(TMP_KEY) // cleanup 호출 여부
        }

        @Test
        @DisplayName("이미지 크기가 PROBE_BYTES보다 작으면 전체 크기만큼만 읽는다.")
        fun shouldReadOnlyActualSizeWhenSmallerThanProbeBytes() {
            // given
            val smallSize = 1024L
            val headInfo = ImageHeadInfoFixture().create(smallSize)
            val smallImageBytes = ByteArray(smallSize.toInt())
            given(s3Provider.getHeadByKey(TMP_KEY)).willReturn(headInfo)
            given(s3Provider.readPrefix(TMP_KEY, smallSize.toInt())).willReturn(smallImageBytes)
            given(tikaProvider.detectMime(smallImageBytes)).willReturn(VALID_MIME)
            given(cdnProperties.domain).willReturn("test-cdn.cloudfront.net")

            // when
            val result = imageService.confirm(TMP_KEY)

            // then
            assertThat(result).isNotNull
            assertThat(result).startsWith("test-cdn.cloudfront.net/")
            assertThat(result).contains(FINAL_KEY)
            verify(s3Provider).readPrefix(TMP_KEY, smallSize.toInt())
        }

        @Test
        @DisplayName("유효한 이미지를 검증하고 CDN URL을 반환한다.")
        fun shouldConfirmValidImageAndReturnCdnUrl() {
            // given
            val headInfo = fixture.create()
            given(s3Provider.getHeadByKey(TMP_KEY)).willReturn(headInfo)
            given(s3Provider.readPrefix(TMP_KEY, VALID_CONTENT_LENGTH.toInt())).willReturn(JPEG_HEADER_BYTES)
            given(tikaProvider.detectMime(JPEG_HEADER_BYTES)).willReturn(VALID_MIME)
            given(cdnProperties.domain).willReturn("test-cdn.cloudfront.net")

            // when
            val result = imageService.confirm(TMP_KEY)

            // then
            assertThat(result).isNotNull
            assertThat(result).startsWith("test-cdn.cloudfront.net/")
            assertThat(result).contains(FINAL_KEY)
            verify(s3Provider).getHeadByKey(TMP_KEY)
            verify(s3Provider).readPrefix(TMP_KEY, VALID_CONTENT_LENGTH.toInt())
            verify(tikaProvider).detectMime(JPEG_HEADER_BYTES)
            verify(s3Provider).copyByKey(TMP_KEY, FINAL_KEY)
        }
    }

    @Nested
    @DisplayName("cancel 메서드는")
    inner class Cancel {
        @Test
        @DisplayName("업로드된 키들을 삭제한다.")
        fun shouldDeleteUploadedKeys() {
            // given
            val uploadedKeys = listOf(TMP_KEY, "tmp/profile/12345/test2.jpg")

            // when
            imageService.cancel(uploadedKeys)

            // then
            verify(s3Provider).deleteByKeys(uploadedKeys)
        }

        @Test
        @DisplayName("빈 리스트로 호출해도 정상 동작한다.")
        fun shouldHandleEmptyList() {
            // given
            val emptyKeys = emptyList<String>()

            // when
            imageService.cancel(emptyKeys)

            // then
            verify(s3Provider).deleteByKeys(emptyKeys)
        }
    }

    @Nested
    @DisplayName("cleanup 메서드는")
    inner class Cleanup {
        @Test
        @DisplayName("URL이 null이면 삭제하지 않는다.")
        fun shouldNotDeleteWhenUrlIsNull() {
            // given
            given(cdnProperties.domain).willReturn(IMAGE_BASE_URL)

            // when
            imageService.cleanup(null)

            // then
            verify(s3Provider, never()).deleteByKey(anyString())
        }

        @Test
        @DisplayName("URL이 비어있다면 삭제하지 않는다.")
        fun shouldNotDeleteWhenUrlIsEmpty() {
            // given
            given(cdnProperties.domain).willReturn(IMAGE_BASE_URL)

            // when
            imageService.cleanup("")

            // then
            verify(s3Provider, never()).deleteByKey(anyString())
        }

        @Test
        @DisplayName("잘못된 CDN 도메인이면 삭제하지 않는다.")
        fun shouldNotDeleteWhenCdnDomainIsMismatched() {
            // given
            given(cdnProperties.domain).willReturn(IMAGE_BASE_URL)

            // when
            imageService.cleanup("https://wrong-cdn.com/members/1/test.jpg")

            // then
            verify(s3Provider, never()).deleteByKey(anyString())
        }

        @Test
        @DisplayName("유효하지 않은 URL 형식이면 삭제하지 않는다.")
        fun shouldNotDeleteWhenUrlFormatIsInvalid() {
            // given
            given(cdnProperties.domain).willReturn(IMAGE_BASE_URL)

            // when
            imageService.cleanup("invalid-url")

            // then
            verify(s3Provider, never()).deleteByKey(anyString())
        }

        @Test
        @DisplayName("유효한 CDN URL에서 키를 추출하고 이미지를 삭제한다.")
        fun shouldExtractKeyAndDeleteImage() {
            // given
            given(cdnProperties.domain).willReturn(IMAGE_BASE_URL)

            // when
            imageService.cleanup("$IMAGE_BASE_URL/$EXTRACTED_KEY")

            // then
            verify(s3Provider).deleteByKey(EXTRACTED_KEY)
        }
    }

    @Nested
    @DisplayName("cleanupBatch 메서드는")
    inner class CleanupBatch {
        @Test
        @DisplayName("빈 리스트로 호출하면 아무 작업도 수행하지 않는다.")
        fun shouldDoNothingWhenListIsEmpty() {
            // when
            imageService.cleanupBatch(emptyList())

            // then
            verify(s3Provider, never()).deleteByKeys(any())
        }

        @Test
        @DisplayName("일부 이미지 삭제 실패 시 실패한 키를 수집한다.")
        fun shouldCollectFailedKeysWhenSomeDeletionsFail() {
            // given
            val imageUrls = listOf(VALID_IMAGE_URL1, VALID_IMAGE_URL2)
            val keys = listOf(VALID_KEY1, VALID_KEY2)
            given(cdnProperties.domain).willReturn(IMAGE_BASE_URL)
            given(s3Provider.deleteByKeys(keys)).willReturn(CleanupImagesResult(1, listOf(VALID_KEY2)))

            // when
            imageService.cleanupBatch(imageUrls)

            // then
            verify(s3Provider).deleteByKeys(keys)
        }

        @Test
        @DisplayName("1000개 이상의 이미지 URL이 들어오면 배치로 나누어 처리한다.")
        fun shouldSplitBatchWhenUrlsExceedMaxBatch() {
            // given
            val imageUrls = List(1500) { "$IMAGE_BASE_URL/members/1/image$it.jpg" }
            given(cdnProperties.domain).willReturn(IMAGE_BASE_URL)
            given(s3Provider.deleteByKeys(any())).willReturn(CleanupImagesResult(1000, emptyList()))

            // when
            imageService.cleanupBatch(imageUrls)

            // then
            verify(s3Provider, atLeast(2)).deleteByKeys(any())
        }

        @Test
        @DisplayName("중복된 이미지 URL은 한 번만 처리한다.")
        fun shouldDeleteDuplicateUrlsOnlyOnceWhenCleanupBatchIsCalled() {
            // given
            val imageUrls = listOf(VALID_IMAGE_URL1, VALID_IMAGE_URL1, VALID_IMAGE_URL2)
            val keys = listOf(VALID_KEY1, VALID_KEY2)
            given(cdnProperties.domain).willReturn(IMAGE_BASE_URL)
            given(s3Provider.deleteByKeys(keys)).willReturn(CleanupImagesResult(2, emptyList()))

            // when
            imageService.cleanupBatch(imageUrls)

            // then
            verify(s3Provider).deleteByKeys(keys)
        }

        @Test
        @DisplayName("잘못된 CDN 도메인을 가진 URL은 필터링하여 처리한다")
        fun shouldFilterUrlsWhenCdnDomainIsInvalid() {
            // given
            val imageUrls = listOf(VALID_IMAGE_URL1, "https://wrong-cdn.com/members/1/image1.jpg")
            val keys = listOf(VALID_KEY1)
            given(cdnProperties.domain()).willReturn(IMAGE_BASE_URL)
            given(s3Provider.deleteByKeys(keys)).willReturn(CleanupImagesResult(1, emptyList()))

            // when
            imageService.cleanupBatch(imageUrls)

            // then
            verify(s3Provider).deleteByKeys(keys)
        }

        @Test
        @DisplayName("유효한 이미지 URL 리스트로 배치 삭제를 수행한다.")
        fun shouldDeleteImagesInBatchWhenUrlsAreValid() {
            // given
            val imageUrls = listOf(VALID_IMAGE_URL1, VALID_IMAGE_URL2)
            val keys = listOf(VALID_KEY1, VALID_KEY2)
            given(cdnProperties.domain).willReturn(IMAGE_BASE_URL)
            given(s3Provider.deleteByKeys(keys)).willReturn(CleanupImagesResult(2, emptyList()))

            // when
            imageService.cleanupBatch(imageUrls)

            // then
            verify(s3Provider).deleteByKeys(keys)
        }
    }

    @Nested
    @DisplayName("publishCleanupBatchEvent 메서드는")
    inner class PublishCleanupBatchEvent {
        @Test
        @DisplayName("빈 리스트로 호출하면 이벤트는 발행되지만 내부에서 처리되지 않는다.")
        fun shouldNotPublishEventWhenListIsEmpty() {
            // when
            imageService.publishCleanupBatchEvent(emptyList())

            // then
            verify(imageEventPublisher).publishCleanupBatch(emptyList())
        }

        @Test
        @DisplayName("null 리스트로 호출하면 이벤트는 발행되지만 내부에서 처리되지 않는다.")
        fun shouldNotPublishEventWhenListIsNull() {
            // when
            imageService.publishCleanupBatchEvent(null)

            // then
            verify(imageEventPublisher).publishCleanupBatch(null)
        }

        @Test
        @DisplayName("유효한 이미지 URL 리스트로 이벤트를 발행한다.")
        fun shouldPublishEventWhenImageUrlsAreValid() {
            // given
            val imageUrls = listOf("https://cdn.example.com/members/1/image1.jpg", "https://cdn.example.com/members/1/image2.jpg")

            // when
            imageService.publishCleanupBatchEvent(imageUrls)

            // then
            verify(imageEventPublisher).publishCleanupBatch(imageUrls)
        }
    }
}
