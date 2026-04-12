package org.github.guardjo.mypocketwebtoon.admin.config;

import lombok.RequiredArgsConstructor;
import org.github.guardjo.mypocketwebtoon.admin.config.properties.LocalStorageProperties;
import org.github.guardjo.mypocketwebtoon.admin.util.FileStorageUploader;
import org.github.guardjo.mypocketwebtoon.admin.util.LocalStorageUploader;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationPropertiesScan("org.github.guardjo.mypocketwebtoon.admin.config.properties")
@RequiredArgsConstructor
public class BaseConfig {
    private final LocalStorageProperties localStorageProperties;

    @Bean
    public FileStorageUploader fileStorageUploader() {
        // TODO 추후 profile 별 별도 uploader 구현체 주입하기
        return new LocalStorageUploader(localStorageProperties);
    }
}
