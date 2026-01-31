package com.dfbs.app.infra.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger configuration for frontend contract.
 * When Spring Security is added: permitAll for /v3/api-docs/**, /swagger-ui/**, /swagger-ui.html.
 */
@Configuration
public class OpenApiConfig {

    private static Schema<?> errorResultSchema() {
        return new Schema<>()
                .addProperty("message", new StringSchema().description("Human-readable error message").example("Quote not found"))
                .addProperty("machineCode", new StringSchema()
                        .description("Machine code for frontend logic")
                        .example("QUOTA_EXCEEDED"));
    }

    @Bean
    public OpenAPI customOpenAPI() {
        final String bearerAuth = "BearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("DFBS Backend API")
                        .version("v1.0")
                        .description("Contract for Frontend"))
                .addSecurityItem(new SecurityRequirement().addList(bearerAuth))
                .components(new Components()
                        .addSecuritySchemes(bearerAuth,
                                new SecurityScheme()
                                        .name(bearerAuth)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }

    @Bean
    public OpenApiCustomizer errorResultSchemaCustomizer() {
        return openApi -> {
            if (openApi.getComponents() == null) {
                openApi.setComponents(new Components());
            }
            openApi.getComponents().addSchemas("ErrorResult", errorResultSchema());
        };
    }

    @Bean
    public GroupedOpenApi v1Api() {
        return GroupedOpenApi.builder()
                .group("v1")
                .pathsToMatch("/api/v1/**")
                .build();
    }
}
