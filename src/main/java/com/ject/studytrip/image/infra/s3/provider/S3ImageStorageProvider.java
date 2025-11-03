package com.ject.studytrip.image.infra.s3.provider;

import com.ject.studytrip.image.application.dto.CleanupImagesResult;
import com.ject.studytrip.image.infra.s3.client.S3ImageStorageClient;
import com.ject.studytrip.image.infra.s3.dto.ImageHeadInfo;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3ImageStorageProvider {
    private final S3ImageStorageClient s3Client;

    public String issuePresignedUrl(String key) {
        PresignedPutObjectRequest presignPut = s3Client.presignPut(key);
        return presignPut.url().toString();
    }

    public ImageHeadInfo getHeadByKey(String key) {
        HeadObjectResponse head = s3Client.getHeadObject(key);
        return ImageHeadInfo.of(head.contentLength());
    }

    public byte[] readPrefix(String key, int maxBytes) {
        String range = "bytes=0-" + (maxBytes - 1);
        return s3Client.getObjectAsBytes(key, range).asByteArray();
    }

    public void deleteByKey(String key) {
        s3Client.deleteObject(key);
    }

    public CleanupImagesResult deleteByKeys(List<String> keys) {
        List<ObjectIdentifier> objects =
                keys.stream().map(key -> ObjectIdentifier.builder().key(key).build()).toList();
        int attempts = objects.size();

        try {
            DeleteObjectsResponse response = s3Client.deleteObjects(objects);
            List<String> failedKeys =
                    response.errors() == null
                            ? List.of()
                            : response.errors().stream()
                                    .map(S3Error::key)
                                    .filter(Objects::nonNull)
                                    .filter(key -> !key.isBlank())
                                    .distinct()
                                    .toList();
            int success = attempts - failedKeys.size();

            return CleanupImagesResult.of(success, failedKeys);
        } catch (S3Exception | SdkClientException e) {
            // S3 삭제는 멱등이기 때문에 키가 존재하지 않거나, 중복이여도 에러가 발생하지 않음
            // 삭제 시 발생하는 에러는 보통 S3 내부 서버 문제(IO/네트워크) 혹은 인증/자격, 권한, 정책, 상태 등으로 발생
            // 따라서 요청 레벨 실패로 간주하고 배치를 전체 실패로 처리
            log.warn("S3 deleteObjects request failure: {}", e.getMessage(), e);
            return CleanupImagesResult.of(0, keys);
        }
    }

    public void copyByKey(String tmpKey, String finalKey) {
        s3Client.copyObject(tmpKey, finalKey);
        s3Client.deleteObject(tmpKey);
    }
}
