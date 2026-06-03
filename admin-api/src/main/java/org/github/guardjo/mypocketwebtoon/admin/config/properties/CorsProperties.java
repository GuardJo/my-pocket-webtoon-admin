package org.github.guardjo.mypocketwebtoon.admin.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties("cors")
public record CorsProperties(
        List<String> allowedOrigins,
        List<String> allowedMethods,
        List<String> allowedHeaders,
        boolean allowCredentials,
        long maxAge
) {
}
