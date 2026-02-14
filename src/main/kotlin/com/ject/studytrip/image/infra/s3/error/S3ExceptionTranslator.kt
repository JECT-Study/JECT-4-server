package com.ject.studytrip.image.infra.s3.error

import com.ject.studytrip.global.exception.CustomException
import org.slf4j.LoggerFactory
import software.amazon.awssdk.core.exception.SdkClientException
import software.amazon.awssdk.core.exception.SdkException
import software.amazon.awssdk.services.s3.model.S3Exception

object S3ExceptionTranslator {
    private val log = LoggerFactory.getLogger(S3ExceptionTranslator::class.java)

    fun <T> executeWithExceptionTranslation(operation: () -> T): T {
        try {
            return operation()
        } catch (e: S3Exception) {
            log.error("S3 service error: {}", e.message, e)
        } catch (e: SdkClientException) {
            log.error("S3 client error: {}", e.message, e)
        } catch (e: SdkException) {
            log.error("AWS SDK error: {}", e.message, e)
        } catch (e: Exception) {
            log.error("Exception: {}", e.message, e)
        }
        throw CustomException(S3ErrorCode.S3_STORAGE_SERVER_ERROR)
    }
}
