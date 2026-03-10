package com.renx.mg.request.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.InputStream;
import java.time.Duration;
import java.util.UUID;

@Service
public class AttachmentStorageService {

    private static final Logger log = LoggerFactory.getLogger(AttachmentStorageService.class);

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucket;
    private final int presignedExpirationMinutes;

    public AttachmentStorageService(@Autowired(required = false) S3Client s3Client,
                                    @Value("${mg.attachments.bucket:}") String bucket,
                                    @Value("${mg.attachments.region:us-east-1}") String region,
                                    @Value("${mg.attachments.presigned-expiration-minutes:60}") int presignedExpirationMinutes) {
        this.s3Client = s3Client;
        String bucketTrimmed = bucket != null ? bucket.trim() : "";
        this.bucket = bucketTrimmed;
        // Presigner debe usar la misma región que el bucket para que las URLs pre-firmadas funcionen
        String regionTrimmed = region != null ? region.trim() : "us-east-1";
        this.s3Presigner = (s3Client != null) ? S3Presigner.builder().region(Region.of(regionTrimmed)).build() : null;
        this.presignedExpirationMinutes = presignedExpirationMinutes;
    }

    public boolean isEnabled() {
        return !bucket.isEmpty();
    }

    /**
     * Uploads a file to S3 and returns the storage key.
     * Key format: requests/{requestId}/{uuid}.{ext}
     */
    public String upload(Long requestId, InputStream inputStream, String contentType, long contentLength) {
        if (!isEnabled() || s3Client == null) {
            log.warn("S3 attachments disabled (mg.attachments.bucket not set); skipping upload");
            return null;
        }
        String ext = contentExtension(contentType);
        String key = "requests/" + requestId + "/" + UUID.randomUUID() + ext;
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .contentLength(contentLength)
                .build();
        s3Client.putObject(request, RequestBody.fromInputStream(inputStream, contentLength));
        log.info("Uploaded attachment to s3://{}/{}", bucket, key);
        return key;
    }

    /**
     * Returns a presigned GET URL for the given storage key (valid for configured expiration).
     */
    public String getPresignedUrl(String storageKey) {
        if (!isEnabled() || s3Presigner == null || storageKey == null || storageKey.isEmpty()) {
            return null;
        }
        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(storageKey)
                .build();
        PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(b -> b
                .getObjectRequest(getRequest)
                .signatureDuration(Duration.ofMinutes(presignedExpirationMinutes)));
        return presigned.url().toString();
    }

    private static String contentExtension(String contentType) {
        if (contentType == null) return "";
        if (contentType.contains("jpeg") || contentType.contains("jpg")) return ".jpg";
        if (contentType.contains("png")) return ".png";
        if (contentType.contains("webp")) return ".webp";
        return "";
    }
}
