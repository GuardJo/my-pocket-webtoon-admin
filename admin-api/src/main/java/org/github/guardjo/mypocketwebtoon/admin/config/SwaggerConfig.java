package org.github.guardjo.mypocketwebtoon.admin.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        String jwtAuth = "jwtAuth";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtAuth);

        Components components = new Components()
                .addSecuritySchemes(jwtAuth, new SecurityScheme()
                        .name(jwtAuth)
                        .type(SecurityScheme.Type.HTTP)
                        .bearerFormat("JWT")
                        .scheme("bearer"));

        return new OpenAPI()
                .addSecurityItem(securityRequirement)
                .components(components)
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
