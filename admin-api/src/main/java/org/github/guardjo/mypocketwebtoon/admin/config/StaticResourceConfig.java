package org.github.guardjo.mypocketwebtoon.admin.config;

import lombok.RequiredArgsConstructor;
import org.github.guardjo.mypocketwebtoon.admin.config.properties.LocalStorageProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@RequiredArgsConstructor
public class StaticResourceConfig implements WebMvcConfigurer {
    private final LocalStorageProperties localStorageProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String handlerPattern = appendWildcard(localStorageProperties.urlPrefix());
        Path uploadRootPath = Paths.get(localStorageProperties.uploadPath()).toAbsolutePath().normalize();

        registry.addResourceHandler(handlerPattern)
                .addResourceLocations(uploadRootPath.toUri().toString());
    }

    private String appendWildcard(String urlPrefix) {
        if (urlPrefix.endsWith("/")) {
            return urlPrefix + "**";
        }
        return urlPrefix + "/**";
    }
}
