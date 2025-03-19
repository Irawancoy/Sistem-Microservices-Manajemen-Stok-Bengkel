package com.microservices.smmsb_inventory_service.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${spring.openapi.title}")
    private String title;

    @Value("${spring.openapi.description}")
    private String description;

    @Value("${spring.openapi.version}")
    private String version;

    @Value("${spring.openapi.contact.name}")
    private String contactName;

    @Value("${spring.openapi.contact.url}")
    private String contactUrl;

    @Value("${spring.openapi.contact.email}")
    private String contactEmail;

    @Value("${spring.openapi.license.name}")
    private String licenseName;

    @Value("${spring.openapi.license.url}")
    private String licenseUrl;

    @Value("${server.port}")
    private int serverPort;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .addSecurityItem(new SecurityRequirement().addList("X-Session-Id"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", createAPIKeyScheme())
                        .addSecuritySchemes("X-Session-Id", createSessionIdScheme()))
                .info(new Info().title(title)
                        .description(description)
                        .version(version)
                        .contact(new Contact().name(contactName).url(contactUrl).email(contactEmail))
                        .license(new License().name(licenseName).url(licenseUrl)))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("API Gateway"),
                        new Server().url("http://localhost:8080/inventory-service")
                                .description("User Inventory via API Gateway"),
                        new Server().url("http://localhost:" + serverPort + "/user-service")
                                .description("Direct Inventory Service")));
    

    }

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer");
    }

    private SecurityScheme createSessionIdScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("X-Session-Id");
    }
}
