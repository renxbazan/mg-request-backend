package com.renx.mg.request.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

import java.net.URI;

@Configuration
public class S3Config {

    private static final Logger log = LoggerFactory.getLogger(S3Config.class);
    private static final String BUCKET_TEST = "mg-work-request-test";
    private static final Region REGION_US_EAST_2 = Region.US_EAST_2;

    @Value("${mg.attachments.bucket:}")
    private String bucket;

    @Value("${mg.attachments.region:us-west-2}")
    private String region;

    @Bean
    @ConditionalOnExpression("!'${mg.attachments.bucket:}'.trim().isEmpty()")
    public S3Client s3Client() {
        String bucketTrimmed = bucket != null ? bucket.trim() : "";
        boolean isTestBucket = BUCKET_TEST.equalsIgnoreCase(bucketTrimmed);
        Region effectiveRegion = isTestBucket ? REGION_US_EAST_2 : Region.of(region != null ? region.trim() : "us-east-2");
        log.info("S3 client: bucket={}, region={}", bucketTrimmed, effectiveRegion.id());
        S3ClientBuilder builder = S3Client.builder().region(effectiveRegion);
        if (isTestBucket) {
            builder.endpointOverride(URI.create("https://s3." + REGION_US_EAST_2.id() + ".amazonaws.com"));
        }
        return builder.build();
    }
}
