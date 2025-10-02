package com.ject.studytrip.image.infra.s3.error;

import com.ject.studytrip.global.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
public class S3ExceptionTranslator {

    public static <T> T executeWithExceptionTranslation(S3Operation<T> operation) {
        try {
            return operation.execute();
        } catch (S3Exception e) {
            log.error("S3 service error: {}", e.getMessage(), e);
        } catch (SdkClientException e) {
            log.error("S3 client error: {}", e.getMessage(), e);
        } catch (SdkException e) {
            log.error("AWS SDK error: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage(), e);
        }

        throw new CustomException(S3ErrorCode.S3_STORAGE_SERVER_ERROR);
    }

    @FunctionalInterface
    public interface S3Operation<T> {
        T execute() throws Exception;
    }
}
