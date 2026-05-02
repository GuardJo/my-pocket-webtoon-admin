package org.github.guardjo.mypocketwebtoon.admin.config;

import lombok.RequiredArgsConstructor;
import org.github.guardjo.mypocketwebtoon.admin.config.properties.LocalStorageProperties;
import org.github.guardjo.mypocketwebtoon.admin.config.properties.R2StorageProperties;
import org.github.guardjo.mypocketwebtoon.admin.util.FileStorageUploader;
import org.github.guardjo.mypocketwebtoon.admin.util.LocalStorageUploader;
import org.github.guardjo.mypocketwebtoon.admin.util.R2StorageUploaderImpl;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@ConfigurationPropertiesScan("org.github.guardjo.mypocketwebtoon.admin.config.properties")
@RequiredArgsConstructor
public class BaseConfig {
    private final LocalStorageProperties localStorageProperties;
    private final R2StorageProperties r2StorageProperties;

    @Bean
    @Profile("local")
    public FileStorageUploader localStorageUploader() {
        return new LocalStorageUploader(localStorageProperties);
    }

    @Bean
    @Profile("!local")
    public FileStorageUploader r2StorageUploader() {
        return new R2StorageUploaderImpl(r2StorageProperties);
    }
}
