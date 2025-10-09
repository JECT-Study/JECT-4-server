package com.ject.studytrip.image.application.service;

import static com.ject.studytrip.image.fixture.ImageTestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.ject.studytrip.BaseUnitTest;
import com.ject.studytrip.global.config.properties.CdnProperties;
import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.image.application.dto.PresignedImageInfo;
import com.ject.studytrip.image.domain.error.ImageErrorCode;
import com.ject.studytrip.image.fixture.ImageHeadInfoFixture;
import com.ject.studytrip.image.infra.s3.dto.ImageHeadInfo;
import com.ject.studytrip.image.infra.s3.error.S3ErrorCode;
import com.ject.studytrip.image.infra.s3.provider.S3ImageStorageProvider;
import com.ject.studytrip.image.infra.tika.provider.TikaImageProbeProvider;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("ImageService 단위 테스트")
class ImageServiceTest extends BaseUnitTest {

    @InjectMocks private ImageService imageService;
    @Mock private S3ImageStorageProvider s3Provider;
    @Mock private TikaImageProbeProvider tikaProvider;
    @Mock private CdnProperties cdnProperties;

    @Nested
    @DisplayName("presign 메서드는")
    class Presign {

        @Test
        @DisplayName("유효한 파라미터로 presigned URL을 발급한다")
        void shouldIssuePresignedUrlWithValidParameters() {
            // given
            given(s3Provider.issuePresignedUrl(anyString())).willReturn(PRESIGNED_URL);

            // when
            PresignedImageInfo info =
                    imageService.presign(VALID_KEY_PREFIX, VALID_ID, ORIGINAL_FILENAME);

            // then
            assertThat(info.presignedUrl()).isEqualTo(PRESIGNED_URL);
            verify(s3Provider).issuePresignedUrl(anyString());
        }

        @Test
        @DisplayName("유효하지 않은 키 Prefix로 호출하면 예외가 발생한다")
        void shouldThrowExceptionWhenKeyPrefixIsInvalid() {
            // when & then
            assertThatThrownBy(() -> imageService.presign(null, VALID_ID, ORIGINAL_FILENAME))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ImageErrorCode.INVALID_IMAGE_KEY_PREFIX.getMessage());
        }

        @Test
        @DisplayName("존재하지 않는 키 Prefix로 호출하면 예외가 발생한다")
        void shouldThrowExceptionWhenKeyPrefixDoesNotExist() {
            // when & then
            assertThatThrownBy(
                            () ->
                                    imageService.presign(
                                            "invalid-key-prefix/", VALID_ID, ORIGINAL_FILENAME))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ImageErrorCode.INVALID_IMAGE_KEY_PREFIX.getMessage());
        }

        @Test
        @DisplayName("유효하지 않은 이미지 파일 확장자라면 예외가 발생한다")
        void shouldThrowExceptionWhenExtensionIsInvalid() {
            // when & then
            assertThatThrownBy(
                            () ->
                                    imageService.presign(
                                            VALID_KEY_PREFIX, VALID_ID, INVALID_ORIGINAL_FILENAME))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ImageErrorCode.INVALID_IMAGE_EXTENSION.getMessage());
        }
    }

    @Nested
    @DisplayName("confirm 메서드는")
    class Confirm {

        @Test
        @DisplayName("유효한 이미지를 검증하고 CDN URL을 반환한다")
        void shouldConfirmValidImageAndReturnCdnUrl() {
            // given
            ImageHeadInfo headInfo = ImageHeadInfoFixture.createImageHeadInfo();
            given(s3Provider.getHeadByKey(TMP_KEY)).willReturn(headInfo);
            given(s3Provider.readPrefix(TMP_KEY, (int) VALID_CONTENT_LENGTH))
                    .willReturn(JPEG_HEADER_BYTES);
            given(tikaProvider.detectMime(JPEG_HEADER_BYTES)).willReturn(VALID_MIME);
            given(cdnProperties.domain()).willReturn("test-cdn.cloudfront.net");

            // when
            String result = imageService.confirm(TMP_KEY);

            // then
            assertThat(result).isNotNull();
            assertThat(result).startsWith("test-cdn.cloudfront.net/");
            assertThat(result).contains(FINAL_KEY);
            verify(s3Provider).getHeadByKey(TMP_KEY);
            verify(s3Provider).readPrefix(TMP_KEY, (int) VALID_CONTENT_LENGTH);
            verify(tikaProvider).detectMime(JPEG_HEADER_BYTES);
            verify(s3Provider).copyByKey(TMP_KEY, FINAL_KEY);
        }

        @Test
        @DisplayName("tmpKey가 null이면 예외가 발생한다")
        void shouldThrowExceptionWhenTmpKeyIsNull() {
            // when & then
            assertThatThrownBy(() -> imageService.confirm(null))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ImageErrorCode.INVALID_IMAGE_KEY.getMessage());

            // cleanup 비호출 여부
            verify(s3Provider, never()).deleteByKey(anyString());
        }

        @Test
        @DisplayName("tmpKey가 빈 문자열이면 예외가 발생한다")
        void shouldThrowExceptionWhenTmpKeyIsBlank() {
            // when & then
            assertThatThrownBy(() -> imageService.confirm(""))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ImageErrorCode.INVALID_IMAGE_KEY.getMessage());

            // cleanup 비호출 여부
            verify(s3Provider, never()).deleteByKey(anyString());
        }

        @Test
        @DisplayName("이미지 크기가 유효하지 않으면 cleanup 후 예외가 발생한다")
        void shouldCleanupAndThrowExceptionWhenImageSizeIsInvalid() {
            // given
            ImageHeadInfo headInfo = ImageHeadInfoFixture.createLargeImageHeadInfo();
            given(s3Provider.getHeadByKey(TMP_KEY)).willReturn(headInfo);

            // when & then
            assertThatThrownBy(() -> imageService.confirm(TMP_KEY))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ImageErrorCode.IMAGE_SIZE_EXCEEDED.getMessage());

            // cleanup 호출 여부
            verify(s3Provider).deleteByKey(TMP_KEY);
        }

        @Test
        @DisplayName("이미지 크기가 0이면 cleanup 후 예외가 발생한다")
        void shouldCleanupAndThrowExceptionWhenImageSizeIsZero() {
            // given
            ImageHeadInfo headInfo = ImageHeadInfoFixture.createEmptyImageHeadInfo();
            given(s3Provider.getHeadByKey(TMP_KEY)).willReturn(headInfo);

            // when & then
            assertThatThrownBy(() -> imageService.confirm(TMP_KEY))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ImageErrorCode.EMPTY_IMAGE.getMessage());

            // cleanup 호출 여부
            verify(s3Provider).deleteByKey(TMP_KEY);
        }

        @Test
        @DisplayName("MIME 타입이 유효하지 않으면 cleanup 후 예외가 발생한다")
        void shouldCleanupAndThrowExceptionWhenMimeIsInvalid() {
            // given
            ImageHeadInfo headInfo = ImageHeadInfoFixture.createImageHeadInfo();
            given(s3Provider.getHeadByKey(TMP_KEY)).willReturn(headInfo);
            given(s3Provider.readPrefix(TMP_KEY, (int) VALID_CONTENT_LENGTH))
                    .willReturn(JPEG_HEADER_BYTES);
            given(tikaProvider.detectMime(JPEG_HEADER_BYTES)).willReturn(INVALID_MIME);

            // when & then
            assertThatThrownBy(() -> imageService.confirm(TMP_KEY))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ImageErrorCode.INVALID_IMAGE_MIME.getMessage());

            // cleanup 호출 여부
            verify(s3Provider).deleteByKey(TMP_KEY);
        }

        @Test
        @DisplayName("빈 문자열 tmpKey로 호출하면 예외가 발생한다")
        void shouldThrowExceptionWhenTmpKeyIsEmptyString() {
            // when & then
            assertThatThrownBy(() -> imageService.confirm(""))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(ImageErrorCode.INVALID_IMAGE_KEY.getMessage());

            // cleanup 비호출 여부
            verify(s3Provider, never()).deleteByKey(anyString());
        }

        @Test
        @DisplayName("S3 작업 중 에러가 발생하면 예외가 발생한다")
        void shouldThrowExceptionWhenS3OperationFails() {
            // given
            given(s3Provider.getHeadByKey(TMP_KEY))
                    .willThrow(new CustomException(S3ErrorCode.S3_STORAGE_SERVER_ERROR));

            // when & then
            assertThatThrownBy(() -> imageService.confirm(TMP_KEY))
                    .isInstanceOf(CustomException.class)
                    .hasMessage(S3ErrorCode.S3_STORAGE_SERVER_ERROR.getMessage());

            // cleanup 비호출 여부
            verify(s3Provider, never()).deleteByKey(anyString());
        }

        @Test
        @DisplayName("이미지 크기가 PROBE_BYTES보다 작으면 전체 크기만큼만 읽는다")
        void shouldReadOnlyActualSizeWhenSmallerThanProbeBytes() {
            // given
            long smallSize = 1024L;
            ImageHeadInfo headInfo = ImageHeadInfoFixture.createImageHeadInfo(smallSize);
            byte[] smallImageBytes = new byte[(int) smallSize];

            given(s3Provider.getHeadByKey(TMP_KEY)).willReturn(headInfo);
            given(s3Provider.readPrefix(TMP_KEY, (int) smallSize)).willReturn(smallImageBytes);
            given(tikaProvider.detectMime(smallImageBytes)).willReturn(VALID_MIME);
            given(cdnProperties.domain()).willReturn("test-cdn.cloudfront.net");

            // when
            String result = imageService.confirm(TMP_KEY);

            // then
            assertThat(result).isNotNull();
            assertThat(result).startsWith("test-cdn.cloudfront.net/");
            assertThat(result).contains(FINAL_KEY);
            verify(s3Provider).readPrefix(TMP_KEY, (int) smallSize);
        }
    }

    @Nested
    @DisplayName("cancel 메서드는")
    class Cancel {

        @Test
        @DisplayName("업로드된 키들을 삭제한다")
        void shouldDeleteUploadedKeys() {
            // given
            List<String> uploadedKeys = Arrays.asList(TMP_KEY, "tmp/profile/12345/test2.jpg");

            // when
            imageService.cancel(uploadedKeys);

            // then
            verify(s3Provider).deleteByKeys(uploadedKeys);
        }

        @Test
        @DisplayName("빈 리스트로 호출해도 정상 동작한다")
        void shouldHandleEmptyList() {
            // given
            List<String> emptyKeys = Arrays.asList();

            // when & then
            imageService.cancel(emptyKeys);

            // then
            verify(s3Provider).deleteByKeys(emptyKeys);
        }
    }

    @Nested
    @DisplayName("cleanup 메서드는")
    class Cleanup {
        private static final String IMAGE_BASE_URL = "https://test-cdn.cloudfront.net";
        private static final String EXTRACTED_KEY = "members/1/test.jpg";

        @Test
        @DisplayName("유효한 CDN URL에서 키를 추출하고 이미지를 삭제한다")
        void shouldExtractKeyAndDeleteImage() {
            // given
            given(cdnProperties.domain()).willReturn(IMAGE_BASE_URL);

            // when
            imageService.cleanup(IMAGE_BASE_URL + "/" + EXTRACTED_KEY);

            // then
            verify(s3Provider).deleteByKey(EXTRACTED_KEY);
        }

        @Test
        @DisplayName("null URL이면 삭제하지 않는다")
        void shouldNotDeleteWhenUrlIsNull() {
            // given
            given(cdnProperties.domain()).willReturn(IMAGE_BASE_URL);

            // when
            imageService.cleanup(null);

            // then
            verify(s3Provider, never()).deleteByKey(anyString());
        }

        @Test
        @DisplayName("빈 URL이면 삭제하지 않는다")
        void shouldNotDeleteWhenUrlIsEmpty() {
            // given
            given(cdnProperties.domain()).willReturn(IMAGE_BASE_URL);

            // when
            imageService.cleanup("");

            // then
            verify(s3Provider, never()).deleteByKey(anyString());
        }

        @Test
        @DisplayName("잘못된 CDN 도메인이면 삭제하지 않는다")
        void shouldNotDeleteWhenCdnDomainMismatch() {
            // given
            given(cdnProperties.domain()).willReturn(IMAGE_BASE_URL);

            // when
            imageService.cleanup("https://wrong-cdn.com/members/1/test.jpg");

            // then
            verify(s3Provider, never()).deleteByKey(anyString());
        }

        @Test
        @DisplayName("유효하지 않은 URL 형식이면 삭제하지 않는다")
        void shouldNotDeleteWhenUrlFormatIsInvalid() {
            // given
            given(cdnProperties.domain()).willReturn(IMAGE_BASE_URL);

            // when
            imageService.cleanup("invalid-url");

            // then
            verify(s3Provider, never()).deleteByKey(anyString());
        }
    }
}
