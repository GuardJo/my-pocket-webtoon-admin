package org.github.guardjo.mypocketwebtoon.admin.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(value = "jwt")
public record JwtProperties(
        String secret,
        long expirationMillis
) {
}
