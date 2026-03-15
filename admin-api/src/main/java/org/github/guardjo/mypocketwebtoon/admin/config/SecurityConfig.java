package org.github.guardjo.mypocketwebtoon.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        // TODO 추후 인증/인가 필터링 구성 에정
        httpSecurity.authorizeHttpRequests(registry -> {
            registry.anyRequest().permitAll();
        });

        return httpSecurity.build();
    }
}
