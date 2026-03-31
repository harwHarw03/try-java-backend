package com.airscope.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SwaggerConfig - sets up the Swagger / OpenAPI documentation UI.
 *
 * After starting the app, visit: http://localhost:8080/swagger-ui.html
 * You can explore and test all endpoints there.
 *
 * We configure it to support JWT — you can paste your token in the
 * "Authorize" button and all requests will include it automatically.
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AirScope API")
                        .description("IoT Environmental Data Collection and Analysis Backend")
                        .version("1.0.0"))
                // Add "Bearer Token" authentication to Swagger UI
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Paste your JWT token here (without 'Bearer ' prefix)")));
    }
}
