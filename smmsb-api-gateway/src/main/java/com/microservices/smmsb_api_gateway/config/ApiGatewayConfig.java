package com.microservices.smmsb_api_gateway.config;

import com.microservices.smmsb_api_gateway.security.JwtAuthFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiGatewayConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public ApiGatewayConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                // Route untuk Auth Service (Login & Register) (Tanpa JWT)
            .route("auth-service", r -> r.path("/api/v1/auth/**")
                        .uri("lb://SMMSB-USER-SERVICE"))

                // Route untuk User Service (Dengan JWT)
            .route("user-service", r -> r.path("/api/v1/users/**")
                        .filters(f -> f.filter(jwtAuthFilter)) 
                        .uri("lb://SMMSB-USER-SERVICE"))

                // Route untuk Inventory Service (Dengan JWT)
            .route("inventory-service", r -> r.path("/api/v1/product-stock/**")
                        .filters(f -> f.filter(jwtAuthFilter))
                        .uri("lb://SMMSB-INVENTORY-SERVICE"))

                // Route untuk Transaction Service (Dengan JWT + Role Check)
            .route("transaction-service", r -> r.path("/api/v1/transactions/**")
                        .filters(f -> f.filter(jwtAuthFilter))
                        .uri("lb://SMMSB-TRANSACTION-SERVICE"))

                // Route untuk Notification Service (Dengan JWT)
                .route("notification-service", r -> r.path("/api/v1/notifications/**")
                        .filters(f -> f.filter(jwtAuthFilter))
                        .uri("lb://SMMSB-NOTIFICATION-SERVICE"))

            .build();
    }
}