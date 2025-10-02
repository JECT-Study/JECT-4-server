package com.ject.studytrip.image.infra.s3.provider;

import com.ject.studytrip.image.infra.s3.client.S3ImageStorageClient;
import com.ject.studytrip.image.infra.s3.dto.ImageHeadInfo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
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

    public void deleteByKeys(List<String> keys) {
        List<ObjectIdentifier> objects =
                keys.stream().map(key -> ObjectIdentifier.builder().key(key).build()).toList();

        s3Client.deleteObjects(objects);
    }

    public void copyByKey(String tmpKey, String finalKey) {
        s3Client.copyObject(tmpKey, finalKey);
        s3Client.deleteObject(tmpKey);
    }
}
