package com.ject.studytrip.image.infra.s3.client;

import com.ject.studytrip.global.config.properties.S3Properties;
import com.ject.studytrip.image.infra.s3.error.S3ExceptionTranslator;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3ImageStorageClient {
    private final S3Properties props;
    private final S3Presigner presigner;
    private final S3Client client;

    public PresignedPutObjectRequest presignPut(String key) {
        return S3ExceptionTranslator.executeWithExceptionTranslation(
                () -> {
                    PutObjectRequest put =
                            PutObjectRequest.builder().bucket(props.bucket()).key(key).build();

                    Duration ttl = Duration.ofMinutes(props.presignExpiresInMinutes());
                    PutObjectPresignRequest req =
                            PutObjectPresignRequest.builder()
                                    .signatureDuration(ttl)
                                    .putObjectRequest(put)
                                    .build();

                    return presigner.presignPutObject(req);
                });
    }

    public HeadObjectResponse getHeadObject(String key) {
        return S3ExceptionTranslator.executeWithExceptionTranslation(
                () -> client.headObject(builder -> builder.bucket(props.bucket()).key(key)));
    }

    public ResponseBytes<GetObjectResponse> getObjectAsBytes(String key, String range) {
        return S3ExceptionTranslator.executeWithExceptionTranslation(
                () ->
                        client.getObjectAsBytes(
                                builder -> builder.bucket(props.bucket()).key(key).range(range)));
    }

    public void deleteObject(String key) {
        S3ExceptionTranslator.executeWithExceptionTranslation(
                () -> client.deleteObject(builder -> builder.bucket(props.bucket()).key(key)));
    }

    public void deleteObjects(List<ObjectIdentifier> objects) {
        S3ExceptionTranslator.executeWithExceptionTranslation(
                () ->
                        client.deleteObjects(
                                builder ->
                                        builder.bucket(props.bucket())
                                                .delete(d -> d.quiet(true).objects(objects))));
    }

    public void copyObject(String tmpKey, String finalKey) {
        S3ExceptionTranslator.executeWithExceptionTranslation(
                () ->
                        client.copyObject(
                                builder ->
                                        builder.sourceBucket(props.bucket())
                                                .sourceKey(tmpKey)
                                                .destinationBucket(props.bucket())
                                                .destinationKey(finalKey)));
    }
}
