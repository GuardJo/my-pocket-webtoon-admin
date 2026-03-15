package org.github.guardjo.mypocketwebtoon.admin.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(apiServer()));
    }

    private Info apiInfo() {
        String description = "my-pocket-webtoon 어드민에서 쓰이는 API 목록 <br/>";

        return new Info()
                .title("my-pocket-webtoon Admin API")
                .description(description)
                .version("1.0.0");
    }

    private Server apiServer() {
        return new Server()
                .url("/");
    }
}
