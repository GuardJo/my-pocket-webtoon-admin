package org.github.guardjo.mypocketwebtoon.admin.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("storage.local")
public record LocalStorageProperties(
        String uploadPath,
        String urlPrefix,
        String publicBaseUrl
) {
}
