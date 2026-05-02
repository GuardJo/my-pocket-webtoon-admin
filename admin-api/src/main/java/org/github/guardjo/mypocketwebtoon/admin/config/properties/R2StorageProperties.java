package org.github.guardjo.mypocketwebtoon.admin.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("storage.r2")
public record R2StorageProperties(
        String accountId,
        String bucketName,
        String publicBaseUrl,
        String accessKey,
        String secretKey
) {
}
