package com.ject.studytrip.image.application.service;

import com.ject.studytrip.global.config.properties.CdnProperties;
import com.ject.studytrip.global.exception.CustomException;
import com.ject.studytrip.global.util.FilenameUtil;
import com.ject.studytrip.image.application.dto.PresignedImageInfo;
import com.ject.studytrip.image.domain.constants.ImageConstants;
import com.ject.studytrip.image.domain.factory.ImageKeyFactory;
import com.ject.studytrip.image.domain.policy.ImagePolicy;
import com.ject.studytrip.image.domain.util.ImageUrlUtil;
import com.ject.studytrip.image.infra.s3.dto.ImageHeadInfo;
import com.ject.studytrip.image.infra.s3.provider.S3ImageStorageProvider;
import com.ject.studytrip.image.infra.tika.provider.TikaImageProbeProvider;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final S3ImageStorageProvider s3Provider;
    private final TikaImageProbeProvider tikaProvider;

    private final CdnProperties cdnProps;

    // Presigned URL 발급
    public PresignedImageInfo presign(String keyPrefix, String id, String originFilename) {
        // 키 prefix 검증
        ImagePolicy.validateKeyPrefix(keyPrefix);

        // 확장자 추출, 검증
        String ext = FilenameUtil.extractExtension(originFilename);
        ImagePolicy.validateExtension(ext);

        // 새로운 파일명 생성
        String filename = FilenameUtil.createNewFilename(ext);

        // 임시 키 생성
        String tmpKey = ImageKeyFactory.createTmpKey(keyPrefix, id, filename);

        // Presigned URL 생성
        String presignedUrl = s3Provider.issuePresignedUrl(tmpKey);

        return PresignedImageInfo.of(tmpKey, presignedUrl);
    }

    // 업로드된 이미지 확정
    // S3 자체 에러 시에는 cleanup 실행 X
    // 이미지 파일 크기, MIME 등 도메인 정책 검증에 실패하면 cleanup 실행
    public String confirm(String tmpKey) {
        // 임시 이미지 키 검증
        ImagePolicy.validateKey(tmpKey);

        // 업로드된 이미지 HEAD 조회
        ImageHeadInfo head = s3Provider.getHeadByKey(tmpKey);

        // 이미지 크기 검증, 검증 실패 시 이미지 삭제
        validateSizeWithCleanup(tmpKey, head.contentLength());

        // MIME 추출 및 판별, 검증 실패 시 이미지 삭제
        validateMimeWithCleanup(tmpKey, head.contentLength());

        // 임시 -> 최종 이미지 복사 및 경로 반환
        return moveToFinalLocation(tmpKey);
    }

    // 업로드 취소
    public void cancel(List<String> uploadedKeys) {
        s3Provider.deleteByKeys(uploadedKeys);
    }

    // 이미지 삭제
    public void cleanup(String imageUrl) {
        ImageUrlUtil.extractKey(cdnProps.domain(), imageUrl).ifPresent(s3Provider::deleteByKey);
    }

    // 이미지 사이즈 검증, 실패 시 삭제
    private void validateSizeWithCleanup(String tmpKey, long contentLength) {
        try {
            ImagePolicy.validateSize(contentLength);
        } catch (CustomException e) {
            cleanupAndThrow(tmpKey, e);
        }
    }

    // 이미지 MIME 추출 및 검증, 실패 시 삭제
    private void validateMimeWithCleanup(String tmpKey, long len) {
        int maxBytes = (int) Math.min(len, ImageConstants.PROBE_BYTES);
        byte[] prefix = s3Provider.readPrefix(tmpKey, maxBytes);
        String mime = tikaProvider.detectMime(prefix);

        try {
            ImagePolicy.validateMime(mime);
        } catch (CustomException e) {
            cleanupAndThrow(tmpKey, e);
        }
    }

    // 최종 경로에 이미지 복사 및 반환
    private String moveToFinalLocation(String tmpKey) {
        String finalKey = ImageKeyFactory.toFinalKey(tmpKey);
        ImagePolicy.validateKey(finalKey);
        s3Provider.copyByKey(tmpKey, finalKey);

        return ImageUrlUtil.build(cdnProps.domain(), finalKey);
    }

    // 삭제 및 예외 처리
    private void cleanupAndThrow(String tmpKey, CustomException exception) {
        s3Provider.deleteByKey(tmpKey);
        throw exception;
    }
}
