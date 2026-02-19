package com.wedit.backend.common.config.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
public class SwaggerConfig {

    @Value("${jwt.access.header}")
    private String accessTokenHeader;

    @Value("${jwt.refresh.header}")
    private String refreshTokenHeader;

    @Bean
    public OpenAPI openAPI() {
        SecurityScheme accessTokenScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name(accessTokenHeader);

        SecurityScheme refreshTokenScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name(refreshTokenHeader);

        return new OpenAPI()
                .info(new Info().title("Wedit Backend API").version("v1"))
                .components(new Components()
                        .addSecuritySchemes(accessTokenHeader, accessTokenScheme)
                        .addSecuritySchemes(refreshTokenHeader, refreshTokenScheme));
    }

    @Bean
    public OpenApiCustomizer securityCustomizer() {
        Set<String> publicPaths = Set.of(
                "/api/v1/member/signup",
                "/api/v1/member/login",
                "/api/v1/member/token-reissue"
        );

        SecurityRequirement requirement = new SecurityRequirement()
                .addList(accessTokenHeader)
                .addList(refreshTokenHeader);

        return openApi -> {
            if (openApi.getPaths() == null) {
                return;
            }
            openApi.getPaths().forEach((path, item) -> {
                if (publicPaths.contains(path)) {
                    item.readOperations().forEach(op -> op.setSecurity(null));
                } else {
                    item.readOperations().forEach(op -> op.addSecurityItem(requirement));
                }
            });
        };
    }
}
