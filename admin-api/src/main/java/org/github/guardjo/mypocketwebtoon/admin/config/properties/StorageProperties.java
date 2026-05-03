package org.github.guardjo.mypocketwebtoon.admin.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("storage")
public record StorageProperties(
        int uploadConcurrency
) {
}
