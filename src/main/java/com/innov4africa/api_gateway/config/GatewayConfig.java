package com.innov4africa.api_gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("ipay_route", r -> r.path("/ipay/**")
                        .uri("https://api.ipay.com"))
                .route("ibanking_route", r -> r.path("/ibanking/**")
                        .uri("https://api.ibanking.com"))
                .build();
    }
}