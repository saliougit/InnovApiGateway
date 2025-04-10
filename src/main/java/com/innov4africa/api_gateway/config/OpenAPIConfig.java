package com.innov4africa.api_gateway.config;

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

/**
 * Configuration pour OpenAPI/Swagger
 */
@Configuration
public class OpenAPIConfig {

    @Value("${api.info.title:API Gateway}")
    private String apiTitle;

    @Value("${api.info.description:API Gateway pour l'intégration des services financiers et utilitaires}")
    private String apiDescription;

    @Value("${api.info.version:1.0}")
    private String apiVersion;

    @Value("${api.info.contact.name:Innov4Africa}")
    private String contactName;

    @Value("${api.info.contact.email:contact@innov4africa.com}")
    private String contactEmail;

    @Value("${api.info.contact.url:https://innov4africa.com}")
    private String contactUrl;

    @Value("${api.info.license.name:Proprietary}")
    private String licenseName;

    @Value("${api.info.license.url:https://innov4africa.com/license}")
    private String licenseUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(apiTitle)
                        .description(apiDescription)
                        .version(apiVersion)
                        .contact(new Contact()
                                .name(contactName)
                                .email(contactEmail)
                                .url(contactUrl))
                        .license(new License()
                                .name(licenseName)
                                .url(licenseUrl)))
                .servers(List.of(
                        new Server().url("/").description("Serveur par défaut")
                ))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization")
                                .description("JWT Token d'authentification")))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }
}