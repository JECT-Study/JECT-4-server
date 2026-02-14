package com.ject.studytrip.image.infra.s3.client

import com.ject.studytrip.global.config.properties.S3Properties
import com.ject.studytrip.image.infra.s3.error.S3ExceptionTranslator
import org.springframework.stereotype.Component
import software.amazon.awssdk.core.ResponseBytes
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse
import software.amazon.awssdk.services.s3.model.GetObjectResponse
import software.amazon.awssdk.services.s3.model.HeadObjectResponse
import software.amazon.awssdk.services.s3.model.ObjectIdentifier
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.time.Duration

@Component
class S3ImageStorageClient(
    private val properties: S3Properties,
    private val presigner: S3Presigner,
    private val client: S3Client,
) {
    fun presignPut(key: String): PresignedPutObjectRequest =
        S3ExceptionTranslator.executeWithExceptionTranslation {
            val put =
                PutObjectRequest
                    .builder()
                    .bucket(properties.bucket)
                    .key(key)
                    .build()

            val ttl = Duration.ofMinutes(properties.presignExpiresInMinutes)

            val req =
                PutObjectPresignRequest
                    .builder()
                    .signatureDuration(ttl)
                    .putObjectRequest(put)
                    .build()

            presigner.presignPutObject(req)
        }

    fun getHeadObject(key: String?): HeadObjectResponse =
        S3ExceptionTranslator.executeWithExceptionTranslation {
            client.headObject { it.bucket(properties.bucket).key(key) }
        }

    fun getObjectAsBytes(
        key: String?,
        range: String,
    ): ResponseBytes<GetObjectResponse> =
        S3ExceptionTranslator.executeWithExceptionTranslation {
            client.getObjectAsBytes { it.bucket(properties.bucket).key(key).range(range) }
        }

    fun deleteObject(key: String?) {
        S3ExceptionTranslator.executeWithExceptionTranslation {
            client.deleteObject { it.bucket(properties.bucket).key(key) }
        }
    }

    fun deleteObjects(objects: List<ObjectIdentifier>): DeleteObjectsResponse =
        client.deleteObjects { it.bucket(properties.bucket).delete { d -> d.quiet(false).objects(objects) } }

    fun copyObject(
        tmpKey: String?,
        finalKey: String?,
    ) {
        S3ExceptionTranslator.executeWithExceptionTranslation {
            client.copyObject {
                it
                    .sourceBucket(properties.bucket)
                    .sourceKey(tmpKey)
                    .destinationBucket(properties.bucket)
                    .destinationKey(finalKey)
            }
        }
    }
}
